package org.halvors.quantum.common.block.particle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.halvors.quantum.Quantum;
import org.halvors.quantum.common.Reference;
import org.halvors.quantum.common.tile.particle.TileFulmination;
import org.halvors.quantum.client.render.BlockRenderingHandler;
import org.halvors.quantum.client.render.ConnectedTextureRenderer;
import org.halvors.quantum.client.render.IBlockCustomRender;
import org.halvors.quantum.client.render.IBlockRenderer;

public class BlockFulmination extends BlockContainer implements IBlockCustomRender {
    public BlockFulmination() {
        super(Material.iron);

        setUnlocalizedName("fulmination");
        setTextureName(Reference.PREFIX + "fulmination");
        setCreativeTab(Quantum.getCreativeTab());
        setHardness(10);
        setResistance(25000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return BlockRenderingHandler.getId();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileFulmination();
    }

    @Override
    public IBlockRenderer getRenderer() {
        return new ConnectedTextureRenderer(this, Reference.PREFIX + "atomic_edge");
    }
}
