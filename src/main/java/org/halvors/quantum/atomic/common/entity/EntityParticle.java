package org.halvors.quantum.atomic.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.halvors.quantum.api.tile.IElectromagnet;
import org.halvors.quantum.atomic.common.Quantum;
import org.halvors.quantum.atomic.common.effect.poison.PoisonRadiation;
import org.halvors.quantum.atomic.common.tile.particle.TileAccelerator;
import org.halvors.quantum.atomic.common.utility.location.Position;

import javax.annotation.Nonnull;
import java.util.List;

public class EntityParticle extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<EnumFacing> movementDirectionParameter = EntityDataManager.createKey(EntityParticle.class, DataSerializers.FACING);

    // Speed by which a particle will turn into anitmatter.
    private static final float antimatterCreationSpeed = 0.9F;

    public Ticket updateTicket;
    public boolean didParticleCollide;
    private int lastTurn = 60;

    private BlockPos movementPos;
    private EnumFacing movementDirection;

    public EntityParticle(World world) {
        super(world);

        ignoreFrustumCheck = true;

        setRenderDistanceWeight(4F);
        setSize(0.3F, 0.3F);
    }

    public EntityParticle(World world, BlockPos pos, BlockPos movementPos, EnumFacing movementDirection) {
        this(world);

        this.movementPos = movementPos;
        this.movementDirection = movementDirection;

        setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /**
     * Checks to see if a new particle can be spawned at the location.
     * @param world - world to check in
     * @param pos - location to check
     * @return true if the spawn location is clear and 2 electromagnets are next to the location
     */
    public static boolean canSpawnParticle(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            int electromagnetCount = 0;

            for (EnumFacing side : EnumFacing.VALUES) {
                if (isElectromagnet(world, pos, side)) {
                    electromagnetCount++;
                }
            }

            return electromagnetCount >= 2;
        }

        return false;
    }

    /**
     * Checks to see if the block is an instance of IElectromagnet and is turned on
     * @param world - world to check in
     * @param pos - position to look for the block/tile
     * @param direction - direction to check in
     * @return true if the location contains an active electromagnet block
     */
    public static boolean isElectromagnet(World world, BlockPos pos, EnumFacing direction) {
        BlockPos checkPos = pos.offset(direction);
        TileEntity tile = world.getTileEntity(checkPos);

        return tile instanceof IElectromagnet && ((IElectromagnet) tile).isRunning();
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeInt(movementPos.getX());
        data.writeInt(movementPos.getY());
        data.writeInt(movementPos.getZ());
        data.writeInt(movementDirection.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        movementPos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
        movementDirection = EnumFacing.getFront(data.readInt());
    }

    @Override
    public void onUpdate() {
        TileEntity tile = world.getTileEntity(movementPos);

        if (tile instanceof TileAccelerator) {
            TileAccelerator tileAccelerator = (TileAccelerator) tile;
            double acceleration = 0.0006;

            // Play sound effects.
            if (ticksExisted % 10 == 0) {
                //world.playSoundAtEntity(this, Reference.PREFIX + "tile.accelerator", 1, (float) (0.6 + (0.4 * (getParticleVelocity() / TileAccelerator.clientParticleVelocity))));
            }

            // Sanity check
            if (tileAccelerator.entityParticle == null) {
                tileAccelerator.entityParticle = this;
            }

            // Force load chunks.
            // TODO: Calculate direction so to only load two chunks instead of 5.
            for (int x = -1; x < 1; x++) {
                for (int z = -1; z < 1; z++) {
                    ForgeChunkManager.forceChunk(updateTicket, new ChunkPos(((int) posX >> 4) + x, ((int) posZ >> 4) + z));
                }
            }

            // Update data watcher.
            if (!world.isRemote) {
                dataManager.set(movementDirectionParameter, movementDirection);
            } else {
                movementDirection = dataManager.get(movementDirectionParameter);
            }

            BlockPos pos = new BlockPos(posX, posY, posZ);

            if ((!isElectromagnet(world, pos, movementDirection.rotateAround(Axis.Y)) ||
                 !isElectromagnet(world, pos, movementDirection.getOpposite().rotateAround(Axis.Y))) && lastTurn <= 0) {
                acceleration = turn();
                motionX = 0;
                motionY = 0;
                motionZ = 0;
                lastTurn = 40;
            }

            lastTurn--;

            // Checks if the current block condition allows the particle to exist.
            if (!canSpawnParticle(world, pos) || isCollided) {
                handleCollisionWithEntity();

                return;
            }

            Position accelerationPos = new Position().offset(movementDirection).scale(acceleration);

            motionX = Math.min(accelerationPos.getX() + motionX, antimatterCreationSpeed);
            motionY = Math.min(accelerationPos.getY() + motionY, antimatterCreationSpeed);
            motionZ = Math.min(accelerationPos.getZ() + motionZ, antimatterCreationSpeed);
            isAirBorne = true;

            lastTickPosX = posX;
            lastTickPosY = posY;
            lastTickPosZ = posZ;

            move(motionX, motionY, motionZ);
            setPosition(posX, posY, posZ);

            if (lastTickPosX == posX && lastTickPosY == posY && lastTickPosZ == posZ && getParticleVelocity() <= 0 && lastTurn <= 0) {
                setDead();
            }

            world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY, posZ, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 0, 0, 0);

            float radius = 0.5F;

            AxisAlignedBB bounds = new AxisAlignedBB(posX - radius, posY - radius, posZ - radius, posX + radius, posY + radius, posZ + radius);
            List<Entity> entitiesNearby = world.getEntitiesWithinAABB(Entity.class, bounds);

            if (entitiesNearby.size() > 1) {
                applyEntityCollision(this);
            }
        } else {
            setDead();
        }
    }

    @Override
    protected void entityInit() {
        dataManager.register(movementDirectionParameter, EnumFacing.SOUTH);

        if (updateTicket == null) {
            updateTicket = ForgeChunkManager.requestTicket(Quantum.getInstance(), world, Type.ENTITY);
            updateTicket.getModData();
            updateTicket.bindEntity(this);
        }
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound tag) {
        movementPos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        movementDirection = EnumFacing.getFront(tag.getByte("direction"));
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound tag) {
        tag.setInteger("x", movementPos.getX());
        tag.setInteger("y", movementPos.getY());
        tag.setInteger("z", movementPos.getZ());
        tag.setByte("direction", (byte) movementDirection.ordinal());
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        handleCollisionWithEntity();
    }

    @Override
    public void setDead() {
        super.setDead();

        ForgeChunkManager.releaseTicket(updateTicket);
    }

    /**
     * Try to move the particle left or right depending on which side is empty.
     *
     * @return The new velocity.
     */
    private double turn() {
        // TODO: Rewrite to allow for up and down turning
        BlockPos pos = new BlockPos(posX, posY, posZ);

        EnumFacing rightDirection = movementDirection.rotateAround(Axis.Y);
        EnumFacing leftDirection = movementDirection.getOpposite().rotateAround(Axis.Y);

        if (world.isAirBlock(pos.offset(rightDirection))) {
            movementDirection = rightDirection;
        } else if (world.isAirBlock(pos.offset(leftDirection))) {
            movementDirection = leftDirection;
        } else {
            setDead();

            return 0;
        }

        setPosition(Math.floor(posX) + 0.5, Math.floor(posY) + 0.5, Math.floor(posZ) + 0.5);

        return getParticleVelocity() - (getParticleVelocity() / Math.min(Math.max(70 * getParticleVelocity(), 4), 30));
    }

    private void handleCollisionWithEntity() {
        //world.playSoundAtEntity(this, Reference.PREFIX + "tile.antimatter", 1.5F, 1F - world.rand.nextFloat() * 0.3F);

        if (!world.isRemote) {
            if (getParticleVelocity() > antimatterCreationSpeed / 2) {
                float radius = 1;
                AxisAlignedBB bounds = new AxisAlignedBB(posX - radius, posY - radius, posZ - radius, posX + radius, posY + radius, posZ + radius);
                List<EntityParticle> entitiesNearby = world.getEntitiesWithinAABB(EntityParticle.class, bounds);

                if (entitiesNearby.size() > 0) {
                    didParticleCollide = true;
                    setDead();

                    return;
                }
            }

            world.createExplosion(this, posX, posY, posZ, (float) getParticleVelocity() * 2.5F, true);
        }

        float radius = 6;
        AxisAlignedBB bounds = new AxisAlignedBB(posX - radius, posY - radius, posZ - radius, posX + radius, posY + radius, posZ + radius);
        List<EntityLivingBase> entitiesNearby = world.getEntitiesWithinAABB(EntityLivingBase.class, bounds);

        for (EntityLivingBase entity : entitiesNearby) {
            PoisonRadiation.getInstance().poisonEntity(entity.getPosition(), entity);
        }

        setDead();
    }

    public double getParticleVelocity() {
        return Math.abs(motionX) + Math.abs(motionY) + Math.abs(motionZ);
    }
}