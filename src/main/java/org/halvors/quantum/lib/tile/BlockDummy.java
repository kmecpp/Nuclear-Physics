package org.halvors.quantum.lib.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.halvors.quantum.common.transform.vector.Cuboid;
import org.halvors.quantum.common.transform.vector.Vector3;
import org.halvors.quantum.lib.render.block.BlockRenderingHandler;
import org.halvors.quantum.lib.utility.inventory.InventoryUtility;

public class BlockDummy extends Block implements ITileEntityProvider {
    public final TileBlock dummyTile;

    public BlockDummy(String modPrefix, CreativeTabs defaultTab, TileBlock dummyTile) {
        super(dummyTile.material);

        this.dummyTile = dummyTile;
        dummyTile.domain = modPrefix;

        setUnlocalizedName(modPrefix + dummyTile.name);
        setTextureName(modPrefix + dummyTile.textureName);

        if (dummyTile.creativeTab != null) {
            setCreativeTab(dummyTile.creativeTab);
        } else {
            setCreativeTab(defaultTab);
        }

        dummyTile.bounds.setBounds(this);

        // TODO: Fix this.
        //opaqueCubeLookup = isOpaqueCube();
        lightOpacity = (isOpaqueCube() ? '�' : 0);

        setHardness(dummyTile.blockHardness);
        setResistance(dummyTile.blockResistance);
        setTickRandomly(dummyTile.tickRandomly);
    }

    public void inject(IBlockAccess access, int x, int y, int z) {
        if (access instanceof World) {
            dummyTile.setWorldObj((World)access);
        }

        dummyTile.access = access;
        dummyTile.xCoord = x;
        dummyTile.yCoord = y;
        dummyTile.zCoord = z;

        TileEntity tile = access.getTileEntity(x, y, z);

        if (tile instanceof TileBlock) {
            ((TileBlock) tile).block = this;
        }
    }

    public void eject() {
        dummyTile.setWorldObj(null);
        dummyTile.xCoord = 0;
        dummyTile.yCoord = 0;
        dummyTile.zCoord = 0;
    }

    public TileBlock getTile(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileBlock) {
            return (TileBlock) tile;
        }

        return dummyTile;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return dummyTile.tile() != null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        try {
            return dummyTile.tile().getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void fillWithRain(World world, int x, int y, int z) {
        inject(world, x, y, z);
        getTile(world, x, y, z).onFillRain();
        eject();
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        inject(world, x, y, z);
        getTile(world, x, y, z).click(player);
        eject();
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        inject(world, x, y, z);
        getTile(world, x, y, z).onAdded();
        eject();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
        inject(world, x, y, z);
        getTile(world, x, y, z).onPlaced(entityLiving, itemStack);
        eject();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        inject(world, x, y, z);
        getTile(world, x, y, z).onRemove(block, metadata);
        eject();

        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return dummyTile.quantityDropped(meta, fortune);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        inject(world, x, y, z);
        getTile(world, x, y, z).onNeighborChanged();
        eject();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        inject(world, x, y, z);
        boolean value = getTile(world, x, y, z).activate(player, side, new Vector3(hitX, hitY, hitZ));
        eject();

        return value;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random par5Random)
    {
        inject(world, x, y, z);
        getTile(world, x, y, z).updateEntity();
        eject();
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        inject(world, x, y, z);
        getTile(world, x, y, z).collide(entity);
        eject();
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
        inject(world, x, y, z);
        Iterable<Cuboid> bounds = getTile(world, x, y, z).getCollisionBoxes(aabb != null ? new Cuboid(aabb).translate(new Vector3(x, y, z).invert()) : null, entity);

        if (bounds != null) {
            for (Cuboid cuboid : bounds) {
                list.add(cuboid.clone().translate(new Vector3(x, y, z)).toAABB());
            }
        }

        eject();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        inject(world, x, y, z);
        Cuboid value = getTile(world, x, y, z).getSelectBounds().clone().translate(new Vector3(x, y, z));
        eject();

        return value.toAABB();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        inject(world, x, y, z);
        Cuboid value = getTile(world, x, y, z).getCollisionBounds().clone().translate(new Vector3(x, y, z));
        eject();

        return value.toAABB();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
        return dummyTile.shouldSideBeRendered(access, x, y, z, side);
    }

    @Override
    public boolean isBlockSolid(IBlockAccess access, int x, int y, int z, int side) {
        inject(access, x, y, z);
        boolean value = getTile(access, x, y, z).isSolid(access, side);
        eject();

        return value;
    }

    @Override
    public int getLightValue(IBlockAccess access, int x, int y, int z) {
        int value = 0;

        if (access != null) {
            inject(access, x, y, z);
            value = getTile(access, x, y, z).getLightValue(access);
            eject();
        }

        return value;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return dummyTile instanceof TileBlock.IComparatorInputOverride;
    }

    @Override
    public boolean isOpaqueCube() {
        if (dummyTile == null) {
            return true;
        }

        return dummyTile.isOpaqueCube;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return dummyTile.normalRender;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return BlockRenderingHandler.ID;
    }


    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        inject(access, x, y, z);
        IIcon value = getTile(access, x, y, z).getIcon(access, side);
        eject();

        return value;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.dummyTile.getIcon(side, meta);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.dummyTile.registerIcons(iconRegister);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        inject(world, x, y, z);
        ItemStack value = getTile(world, x, y, z).getPickBlock(target);
        eject();

        return value;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        inject(world, x, y, z);
        ArrayList<ItemStack> value = getTile(world, x, y, z).getDrops(metadata, fortune);
        eject();

        return value != null ? value : new ArrayList<ItemStack>();
    }

    @Override
    protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack itemStack) {
        if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
            InventoryUtility.dropItemStack(world, new Vector3(x, y, z), itemStack);
        }
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
        dummyTile.getSubBlocks(item, creativeTab, list);
    }

    @Override
    public boolean canProvidePower() {
        return dummyTile.canProvidePower;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side) {
        inject(access, x, y, z);
        int value = getTile(access, x, y, z).getWeakRedstonePower(access, side);
        eject();
        return value;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side) {
        inject(access, x, y, z);
        int value = getTile(access, x, y, z).getStrongRedstonePower(access, side);
        eject();
        return value;
    }

    @Override
    public int getRenderBlockPass() {
        return dummyTile.getRenderBlockPass();
    }

    @Override
    public int tickRate(World world) {
        return dummyTile.tickRate(world);
    }
}