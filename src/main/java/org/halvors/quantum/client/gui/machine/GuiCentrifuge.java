package org.halvors.quantum.client.gui.machine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.halvors.quantum.common.container.machine.ContainerCentrifuge;
import org.halvors.quantum.common.tile.machine.TileCentrifuge;
import org.halvors.quantum.common.tile.machine.TileNuclearBoiler;
import org.halvors.quantum.lib.gui.GuiContainerBase;
import universalelectricity.api.energy.UnitDisplay;

public class GuiCentrifuge extends GuiContainerBase {
    private TileCentrifuge tile;

    public GuiCentrifuge(InventoryPlayer inventoryPlayer, TileCentrifuge tile) {
        super(new ContainerCentrifuge(inventoryPlayer, tile));
        
        this.tile = tile;
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(tile.getInventoryName(), 60, 6, 4210752);

        String displayText;

        if (tile.timer > 0) {
            displayText = "Processing";
        } else if (tile.nengYong()) {
            displayText = "Ready";
        } else {
            displayText = "Idle";
        }

        fontRendererObj.drawString("Status: " + displayText, 70, 50, 4210752);

        renderUniversalDisplay(8, 112, TileNuclearBoiler.DIAN * 20, mouseX, mouseY, UnitDisplay.Unit.WATT);
        renderUniversalDisplay(100, 112, tile.getVoltageInput(null), mouseX, mouseY, UnitDisplay.Unit.VOLTAGE);

        fontRendererObj.drawString("The centrifuge spins", 8, 75, 4210752);
        fontRendererObj.drawString("uranium hexafluoride gas into", 8, 85, 4210752);
        fontRendererObj.drawString("enriched uranium for fission.", 8, 95, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);

        if (isPointInRegion(8, 18, meterWidth, meterHeight, mouseX, mouseY) && tile.gasTank.getFluid() != null) {
            drawTooltip(mouseX - guiLeft, mouseY - guiTop + 10, tile.gasTank.getFluid().getFluid().getLocalizedName(), tile.gasTank.getFluid().amount + " L");
        }
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        drawSlot(80, 25);
        drawSlot(100, 25);
        drawSlot(130, 25, SlotType.BATTERY);

        drawBar(40, 26, (float) tile.timer / (float) TileCentrifuge.SHI_JIAN);

        // Uranium Gas
        drawMeter(8, 18, (float) tile.gasTank.getFluidAmount() / (float) tile.gasTank.getCapacity(), tile.gasTank.getFluid());
        drawSlot(24, 49, SlotType.GAS);
    }
}