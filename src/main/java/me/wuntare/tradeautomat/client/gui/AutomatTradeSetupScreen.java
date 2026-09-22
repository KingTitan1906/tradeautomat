package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.client.gui.util.ScrollController;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import me.wuntare.tradeautomat.model.TradeOffer;
import me.wuntare.tradeautomat.network.SetScrollOffsetPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AutomatTradeSetupScreen extends AbstractContainerScreen<AutomatTradeSetupMenu> {

    private static final int PADDING_LEFT = 8;
    private static final int INPUTS_WIDTH = TradeOffer.MAX_INPUTS * 18;
    private static final int ARROW_GAP = 12;
    private static final int OUTPUTS_WIDTH = TradeOffer.MAX_OUTPUTS * 18;
    private static final int SCROLLBAR_GAP = 16;
    private static final int SCROLLBAR_WIDTH = 14;
    private static final int PADDING_RIGHT = 8;

    public static final int MAIN_PANEL_WIDTH = PADDING_LEFT + INPUTS_WIDTH + ARROW_GAP + OUTPUTS_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH + PADDING_RIGHT; // 326 px

    private final ScrollController scrollController = new ScrollController();

    public AutomatTradeSetupScreen(AutomatTradeSetupMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int panelHeight = this.menu.getDynamicPanelHeight();
        this.leftPos = (this.width - MAIN_PANEL_WIDTH) / 2;
        this.topPos = (this.height - panelHeight) / 2;

        int visibleRows = this.menu.getVisibleRowsCount();
        int playerInvY = 18 + visibleRows * 22 + 10;

        this.inventoryLabelY = playerInvY - 11;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = this.leftPos;
        int y = this.topPos;

        int panelHeight = this.menu.getDynamicPanelHeight();

        GuiRenderUtils.drawMenu(graphics, x, y, MAIN_PANEL_WIDTH, panelHeight);

        GuiRenderUtils.drawTradeSetupMenuSlots(graphics, x, y, this.menu);

        if (this.menu.getBlockEntity() == null) return;

        int unlockedTrades = this.menu.getBlockEntity().getUnlockedTradeOffers();
        int visibleRows = this.menu.getVisibleRowsCount();

        int arrowX = x+1 + PADDING_LEFT + INPUTS_WIDTH;
        for (int i = 0; i < visibleRows; i++) {
            int tradeIndex = this.menu.getScrollOffset() + i;
            if (tradeIndex >= unlockedTrades) break;

            int arrowY = y + 19 + (i * 22);
            GuiRenderUtils.drawArrow(graphics, arrowX, arrowY);
        }

        if (unlockedTrades > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            int scrollbarX = x + PADDING_LEFT + INPUTS_WIDTH + ARROW_GAP + OUTPUTS_WIDTH + SCROLLBAR_GAP;
            renderScrollbar(graphics, scrollbarX, y + 17);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y) {
        GuiRenderUtils.drawScrollbar(
                graphics,
                x,
                y,
                SCROLLBAR_WIDTH,
                84,
                scrollController.getScrollAmount()
        );
    }

    private void updateScrollOffset(int newOffset) {
        if (this.menu.getScrollOffset() != newOffset) {
            this.menu.setScrollOffset(newOffset);
            ClientPlayNetworking.send(new SetScrollOffsetPayload(this.menu.containerId, newOffset));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.menu.getBlockEntity() == null) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int totalTrades = this.menu.getBlockEntity().getUnlockedTradeOffers();
        if (totalTrades > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            scrollController.onMouseScrolled(verticalAmount, totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS);
            int newOffset = scrollController.getRowOffset(totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS);
            updateScrollOffset(newOffset);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.menu.getBlockEntity() == null) return super.mouseDragged(event, dragX, dragY);

        int totalTrades = this.menu.getBlockEntity().getUnlockedTradeOffers();
        if (scrollController.isDragging() && totalTrades > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            scrollController.onMouseDragged(event.y(), this.topPos + 18, 86);
            int newOffset = scrollController.getRowOffset(totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS);
            updateScrollOffset(newOffset);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean button = event.button() == 0;
        int scrollbarX = this.leftPos + PADDING_LEFT + INPUTS_WIDTH + ARROW_GAP + OUTPUTS_WIDTH + SCROLLBAR_GAP;
        int scrollbarY = this.topPos + 18;
        double mouseX = event.x();
        double mouseY = event.y();

        if (this.menu.getBlockEntity() != null && this.menu.getBlockEntity().getUnlockedTradeOffers() > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            if (button && scrollController.onMouseClicked(mouseX, mouseY, scrollbarX, scrollbarY, SCROLLBAR_WIDTH, 86)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            scrollController.onMouseReleased();
        }
        return super.mouseReleased(event);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        int panelHeight = this.menu.getDynamicPanelHeight();
        boolean insideMain = mouseX >= guiLeft && mouseX < guiLeft + MAIN_PANEL_WIDTH
                && mouseY >= guiTop && mouseY < guiTop + panelHeight;

        return !insideMain;
    }
}