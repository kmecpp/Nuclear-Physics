package org.halvors.quantum.common.block.machine;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.halvors.quantum.common.Quantum;
import org.halvors.quantum.common.tile.machine.TileChemicalExtractor;
import org.halvors.quantum.common.tile.machine.TileGasCentrifuge;
import org.halvors.quantum.common.tile.machine.TileNuclearBoiler;
import org.halvors.quantum.common.tile.machine.TileQuantumAssembler;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockMachineModel extends BlockMachine {
    private static final PropertyEnum<EnumModelMachine> type = PropertyEnum.create("type", EnumModelMachine.class);

    public BlockMachineModel() {
        super("machine_model");

        setDefaultState(blockState.getBaseState().withProperty(type, EnumModelMachine.CHEMICAL_EXTRACTOR));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFullCube(IBlockState blockState) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    @Override
    public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTabs, List<ItemStack> list) {
        for (EnumModelMachine type : EnumModelMachine.values()) {
            list.add(new ItemStack(item, 1, type.ordinal()));
        }
    }

    @Override
    @Nonnull
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, type, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(type).ordinal();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack itemStack) {
        world.setBlockState(pos, state.withProperty(type, EnumModelMachine.values()[itemStack.getItemDamage()]), 2);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
        final EnumModelMachine type = EnumModelMachine.values()[metadata];

        return type.getTile();
    }

    public enum EnumModelMachine implements IStringSerializable {
        CHEMICAL_EXTRACTOR("chemical_extractor", TileChemicalExtractor.class),
        GAS_CENTRIFUGE("gas_centrifuge", TileGasCentrifuge.class),
        NUCLEAR_BOILER("nuclear_boiler", TileNuclearBoiler.class),
        QUANTUM_ASSEMBLER("quantum_assembler", TileQuantumAssembler.class);

        private String name;
        private Class<? extends TileEntity> tileClass;

        EnumModelMachine(String name, Class<? extends TileEntity> tileClass) {
            this.name = name;
            this.tileClass = tileClass;
        }

        @Override
        public String getName() {
            return name.toLowerCase();
        }

        public TileEntity getTile() {
            try {
                return tileClass.newInstance();
            } catch (Exception e) {
                Quantum.getLogger().error("Unable to indirectly create tile entity.");
                e.printStackTrace();

                return null;
            }
        }
    }
}