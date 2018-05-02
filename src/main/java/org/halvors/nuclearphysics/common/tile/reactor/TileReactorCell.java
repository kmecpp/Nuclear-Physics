package org.halvors.nuclearphysics.common.tile.reactor;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import org.halvors.nuclearphysics.api.item.IReactorComponent;
import org.halvors.nuclearphysics.api.tile.IReactor;
import org.halvors.nuclearphysics.common.NuclearPhysics;
import org.halvors.nuclearphysics.common.capabilities.fluid.LiquidTank;
import org.halvors.nuclearphysics.common.effect.explosion.ReactorExplosion;
import org.halvors.nuclearphysics.common.event.PlasmaEvent.PlasmaSpawnEvent;
import org.halvors.nuclearphysics.common.grid.thermal.ThermalGrid;
import org.halvors.nuclearphysics.common.grid.thermal.ThermalPhysics;
import org.halvors.nuclearphysics.common.init.ModBlocks;
import org.halvors.nuclearphysics.common.init.ModFluids;
import org.halvors.nuclearphysics.common.init.ModPotions;
import org.halvors.nuclearphysics.common.init.ModSounds;
import org.halvors.nuclearphysics.common.network.packet.PacketTileEntity;
import org.halvors.nuclearphysics.common.tile.TileInventory;
import org.halvors.nuclearphysics.common.tile.reactor.fusion.TilePlasma;
import org.halvors.nuclearphysics.common.type.Position;
import org.halvors.nuclearphysics.common.utility.LanguageUtility;

import java.util.List;

public class TileReactorCell extends TileInventory implements IFluidHandler, IReactor {
    private String name;
    private ItemStack[] inventory;
    private int[] openSlots;

    public static final int radius = 2;
    public static final int meltingPoint = 2000;
    private final int specificHeatCapacity = 1000;
    private final float mass = ThermalPhysics.getMass(1000, 7);

    private float temperature = ThermalPhysics.roomTemperature; // Synced
    private float previousTemperature = temperature;

    private boolean shouldUpdate = false;

    private long internalEnergy = 0;
    private long previousInternalEnergy = 0;
    private int meltdownCounter = 0;
    private int meltdownCounterMaximum = 1000;

    private final LiquidTank tank = new LiquidTank(FluidContainerRegistry.BUCKET_VOLUME * 15) {
        @Override
        public int fill(final FluidStack resource, final boolean doFill) {
            if (resource.isFluidEqual(ModFluids.fluidStackPlasma)) {
                return super.fill(resource, doFill);
            }

            return 0;
        }
    };

    public TileReactorCell() {
        this("reactor_cell");
    }

    public TileReactorCell(final String name) {
        super(1);

        this.name = name;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag) {
        super.readFromNBT(tag);

        temperature = tag.getFloat("temperature");
        tank.readFromNBT(tag.getCompoundTag("tank"));
    }

    @Override
    public void writeToNBT(final NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setFloat("temperature", temperature);
        tag.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void updateEntity() {
        // TODO: Should we do this for fusion reactors as well?
        // Reactor cell plays random idle noises while operating with temperature above boiling water temperature.
        if (worldObj.getWorldTime() % 100 == 0 && temperature >= ThermalPhysics.waterBoilTemperature) {
            float percentage = Math.min(temperature / meltingPoint, 1);

            worldObj.playSoundEffect(xCoord, yCoord, zCoord, ModSounds.REACTOR_CELL, percentage, 1);
        }

        if (!worldObj.isRemote) {
            final FluidStack fluidStack = tank.getFluid();

            if (fluidStack != null && fluidStack.isFluidEqual(ModFluids.fluidStackPlasma)) {
                // Spawn plasma.
                final FluidStack drain = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);

                if (drain != null && drain.amount >= FluidContainerRegistry.BUCKET_VOLUME) {
                    final ForgeDirection spawnDir = ForgeDirection.getOrientation(worldObj.rand.nextInt(3) + 2);
                    final Position spawnPos = new Position(this).offset(spawnDir, 2);

                    if (worldObj.isAirBlock(spawnPos.getIntX(), spawnPos.getIntY(), spawnPos.getIntZ())) {
                        MinecraftForge.EVENT_BUS.post(new PlasmaSpawnEvent(worldObj, spawnPos.getIntX(), spawnPos.getIntY(), spawnPos.getIntZ(), TilePlasma.plasmaMaxTemperature));
                        tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                    }
                }
            } else {
                previousInternalEnergy = internalEnergy;

                // Handle cell rod interactions.
                final ItemStack fuelRod = getStackInSlot(0);

                if (fuelRod != null && fuelRod.getItem() instanceof IReactorComponent) {
                    // Activate rods.
                    final IReactorComponent reactorComponent = (IReactorComponent) fuelRod.getItem();
                    reactorComponent.onReact(fuelRod, this);

                    if (fuelRod.getMetadata() >= fuelRod.getMaxDurability()) {
                        setInventorySlotContents(0, null);
                    }

                    // Emit radiation.
                    if (worldObj.getTotalWorldTime() % 20 == 0 && worldObj.rand.nextFloat() > 0.65) {
                        final List<EntityLiving> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(xCoord - radius * 2, yCoord - radius * 2, zCoord - radius * 2, xCoord + radius * 2, yCoord + radius * 2, zCoord + radius * 2));

                        for (EntityLiving entity : entities) {
                            ModPotions.poisonRadiation.poisonEntity(entity);
                        }
                    }
                }

                // Update the temperature from the thermal grid.
                temperature = ThermalGrid.getTemperature(worldObj, new Position(this));

                // Only a small percentage of the internal energy is used for temperature.
                if ((internalEnergy - previousInternalEnergy) > 0) {
                    float deltaTemperature = ThermalPhysics.getTemperatureForEnergy(mass, specificHeatCapacity, (long) ((internalEnergy - previousInternalEnergy) * 0.15));

                    // Check control rods.
                    for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                        final Position checkPos = new Position(this).offset(side);

                        if (checkPos.getBlock(worldObj) == ModBlocks.blockControlRod) {
                            deltaTemperature /= 2;
                        }
                    }

                    // Add heat to surrounding blocks in the thermal grid.
                    ThermalGrid.addTemperature(worldObj, new Position(this), deltaTemperature);

                    if (previousTemperature != temperature && !shouldUpdate) {
                        shouldUpdate = true;
                        previousTemperature = temperature;
                    }

                    // If temperature is over the melting point of the reactor, either increase counter or melt down.
                    if (previousTemperature >= meltingPoint) {
                        if (meltdownCounter < meltdownCounterMaximum) {
                            meltdownCounter++;
                            shouldUpdate = true;
                        } else {
                            meltdownCounter = 0;
                            meltDown();

                            return;
                        }
                    }

                    // If reactor temperature is below meltingPoint and meltdownCounter is over 0, decrease it.
                    if (previousTemperature < meltingPoint && meltdownCounter > 0) {
                        meltdownCounter--;
                    }
                }

                internalEnergy = 0;

                if (isOverToxic()) {
                    // Randomly leak toxic waste when it is too toxic.
                    final Position leakPos = new Position(this).add(worldObj.rand.nextInt(20) - 10, worldObj.rand.nextInt(20) - 10, worldObj.rand.nextInt(20) - 10);
                    final Block block = worldObj.getBlock(leakPos.getIntX(), leakPos.getIntY(), leakPos.getIntZ());

                    if (block == Blocks.grass) {
                        worldObj.setBlock(leakPos.getIntX(), leakPos.getIntY(), leakPos.getIntZ(), ModBlocks.blockRadioactiveGrass);
                        tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                    } else if (worldObj.isAirBlock(leakPos.getIntX(), leakPos.getIntY(), leakPos.getIntZ()) || block.isReplaceable(worldObj, leakPos.getIntX(), leakPos.getIntY(), leakPos.getIntZ())) {
                        if (fluidStack != null) {
                            worldObj.setBlock(leakPos.getIntX(), leakPos.getIntY(), leakPos.getIntZ(), fluidStack.getFluid().getBlock());
                            tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
                        }
                    }
                }
            }

            if (worldObj.getTotalWorldTime() % 60 == 0 || shouldUpdate) {
                shouldUpdate = false;
                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, blockType);

                NuclearPhysics.getPacketHandler().sendToReceivers(new PacketTileEntity(this), this);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handlePacketData(final ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (worldObj.isRemote) {
            temperature = dataStream.readFloat();
            tank.handlePacketData(dataStream);
        }
    }

    @Override
    public List<Object> getPacketData(final List<Object> objects) {
        super.getPacketData(objects);

        objects.add(temperature);
        tank.getPacketData(objects);

        return objects;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isItemValidForSlot(final int slot, final ItemStack itemStack) {
        return !ModFluids.fluidStackPlasma.isFluidEqual(tank.getFluid()) && getStackInSlot(0) == null && itemStack.getItem() instanceof IReactorComponent;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
        if (resource.isFluidEqual(ModFluids.fluidStackPlasma)) {
            return tank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
        if (resource.isFluidEqual(ModFluids.fluidStackToxicWaste)) {
            return drain(from, resource.amount, doDrain);
        }

        return null;
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(final ForgeDirection from, final Fluid fluid) {
        return fluid.getID() == ModFluids.fluidStackPlasma.getFluidID();
    }

    @Override
    public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
        return fluid.getID() == ModFluids.fluidStackToxicWaste.getFluidID();
    }

    @Override
    public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
        return new FluidTankInfo[] { tank.getInfo() };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void heat(final long energy) {
        internalEnergy = Math.max(internalEnergy + energy, 0);
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public boolean isOverToxic() {
        final FluidStack fluidStack = tank.getFluid();

        return fluidStack != null && fluidStack.amount == tank.getCapacity() && fluidStack.isFluidEqual(ModFluids.fluidStackToxicWaste);
    }

    @Override
    public FluidTank getTank() {
        return tank;
    }

    @Override
    public World getWorldObject() {
        return worldObj;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        return LanguageUtility.transelate(getBlockType().getUnlocalizedName() + ".name");
    }

    private void meltDown() {
        // Make sure the reactor block is destroyed.
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);

        // Create the explosion.
        final ReactorExplosion explosion = new ReactorExplosion(worldObj, null, xCoord, yCoord, zCoord, 9);
        explosion.explode();
    }
}