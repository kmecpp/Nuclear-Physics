package org.halvors.quantum.atomic.common.block.reactor.fission;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.halvors.quantum.atomic.common.block.BlockContainerQuantum;
import org.halvors.quantum.atomic.common.item.block.ItemBlockSaved;
import org.halvors.quantum.atomic.common.item.block.ItemBlockThermometer;
import org.halvors.quantum.atomic.common.tile.reactor.fission.TileThermometer;
import org.halvors.quantum.atomic.common.utility.InventoryUtility;
import org.halvors.quantum.atomic.common.utility.WrenchUtility;
import org.halvors.quantum.atomic.common.utility.transform.vector.Vector3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockThermometer extends BlockContainerQuantum {
    public BlockThermometer() {
        super("thermometer", Material.PISTON);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack itemStack, EnumFacing side, float hitX, float hitY, float hitZ) {
        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileThermometer) {
            final TileThermometer tileThermometer = (TileThermometer) tile;

            if (WrenchUtility.hasUsableWrench(player, pos)) {
                if (player.isSneaking()) {
                    tileThermometer.setThreshold(tileThermometer.getThershold() - 10);
                } else {
                    tileThermometer.setThreshold(tileThermometer.getThershold() + 10);
                }

                return true;
            }

            if (player.isSneaking()) {
                tileThermometer.setThreshold(tileThermometer.getThershold() + 100);
            } else {
                tileThermometer.setThreshold(tileThermometer.getThershold() - 100);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack itemStack) {
        final TileEntity tile = world.getTileEntity(pos);

        // Fetch saved coordinates from ItemBlockThermometer and apply them to the block.
        if (tile instanceof TileThermometer) {
            final TileThermometer tileThermometer = (TileThermometer) tile;
            final ItemBlockThermometer itemBlockThermometer = (ItemBlockThermometer) itemStack.getItem();
            tileThermometer.setTrackCoordinate(itemBlockThermometer.getSavedCoord(itemStack));
        }

        super.onBlockPlacedBy(world, pos, state, entity, itemStack);
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {
            ItemStack itemStack = ItemBlockSaved.getItemStackWithNBT(state, world, pos);
            InventoryUtility.dropItemStack(world, new Vector3(pos.getX(), pos.getY(), pos.getZ()), itemStack);
        }

        return world.setBlockToAir(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    // TODO: Port this.
    /*
    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side) {
        TileEntity tile = access.getTileEntity(x, y, z);

        if (tile instanceof TileThermometer) {
            TileThermometer tileThermometer = (TileThermometer) tile;

            return tileThermometer.isProvidingPower ? 15 : 0;
        }

        return 0;
    }
    */

    @Override
    @Nonnull
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    @Nonnull
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileThermometer();
    }
}