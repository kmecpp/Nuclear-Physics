package org.halvors.nuclearphysics.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.halvors.nuclearphysics.common.Reference;
import org.halvors.nuclearphysics.common.type.EnumColor;
import org.halvors.nuclearphysics.common.utility.LanguageUtility;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemTooltip extends ItemBase {
    public ItemTooltip(final String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
        final String tooltip = getUnlocalizedName(itemStack) + ".tooltip";

        if (LanguageUtility.canTranselate(tooltip)) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                list.add(LanguageUtility.transelate("tooltip." + Reference.ID + ".noShift", EnumColor.AQUA.toString(), EnumColor.GREY.toString()));
            } else {
                list.addAll(LanguageUtility.splitStringPerWord(LanguageUtility.transelate(tooltip), 5));
            }
        }
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        if (itemStack.getHasSubtypes()) {
            return super.getUnlocalizedName(itemStack) + "." + itemStack.getMetadata();
        }

        return super.getUnlocalizedName(itemStack);
    }
}
