package org.halvors.nuclearphysics.client.render.block.reactor;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.halvors.nuclearphysics.client.utility.RenderUtility;
import org.halvors.nuclearphysics.common.tile.reactor.TileThermometer;
import org.halvors.nuclearphysics.common.type.EnumColor;
import org.halvors.nuclearphysics.common.type.Position;
import org.halvors.nuclearphysics.common.unit.UnitDisplay;

@SideOnly(Side.CLIENT)
public class RenderThermometer extends TileEntitySpecialRenderer<TileThermometer> {
    @Override
    public void renderTileEntityAt(final TileThermometer tile, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        GlStateManager.pushMatrix();
        RenderUtility.enableLightmap();

        RenderUtility.renderText((tile.isOverThreshold() ? EnumColor.DARK_RED : EnumColor.BLACK) + UnitDisplay.getTemperatureDisplay(Math.round(tile.getDetectedTemperature())), tile.getFacing(), 0.8F, x, y + 0.1, z);
        RenderUtility.renderText((tile.isOverThreshold() ? EnumColor.DARK_RED : EnumColor.DARK_BLUE) + "Threshold: " + UnitDisplay.getTemperatureDisplay(tile.getThershold()), tile.getFacing(), 1, x, y - 0.1, z);

        final Position trackCoordinate = tile.getTrackCoordinate();

        if (tile.getTrackCoordinate() != null) {
            RenderUtility.renderText(trackCoordinate.getIntX() + ", " + trackCoordinate.getIntY() + ", " + trackCoordinate.getIntZ(), tile.getFacing(), 0.5F, x, y - 0.3, z);
        }

        GlStateManager.popMatrix();
    }
}
