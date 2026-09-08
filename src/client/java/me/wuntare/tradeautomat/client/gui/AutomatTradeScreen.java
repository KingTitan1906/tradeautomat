package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.gui.AutomatTradeMenu;
import me.wuntare.tradeautomat.client.gui.util.ScrollController;
import me.wuntare.tradeautomat.network.ExecuteTradePayload;
import me.wuntare.tradeautomat.network.SetScrollOffsetPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AutomatTradeScreen extends AbstractContainerScreen<AutomatTradeMenu> {
    private static final int MAIN_PANEL_WIDTH = 176;
    private final ScrollController scrollController = new ScrollController();
    private final List<Button> tradeButtons = new ArrayList<>();

    public AutomatTradeScreen(AutomatTradeMenu menu, Inventory inventory, Component title) {
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

        int x = this.leftPos;
        int y = this.topPos;

        this.tradeButtons.clear();

        for (int i = 0; i < visibleRows; i++) {
            final int rowIndex = i;

            Button btn = Button.builder(Component.literal("Trade"), button -> {
                        int rawTradeIndex = this.menu.getActualTradeIndex(rowIndex);
                        if (rawTradeIndex != -1 && this.menu.getBlockEntity() != null) {
                            ClientPlayNetworking.send(new ExecuteTradePayload(this.menu.getBlockEntity().getBlockPos(), rawTradeIndex));
                        }
                    })
                    .bounds(x + 96, y + 18 + i * 22, 38, 16)
                    .build();

            this.tradeButtons.add(btn);
            this.addRenderableWidget(btn);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = this.leftPos;
        int y = this.topPos;

        int panelHeight = this.menu.getDynamicPanelHeight();

        GuiRenderUtils.drawMenu(graphics, x, y, MAIN_PANEL_WIDTH, panelHeight);
        GuiRenderUtils.drawMenuSlots(graphics, x, y, this.menu);

        if (this.menu.getBlockEntity() == null) return;

        int validTrades = this.menu.getValidTradesCount();
        int visibleRows = this.menu.getVisibleRowsCount();

        for (int i = 0; i < visibleRows; i++) {
            int rawTradeIndex = this.menu.getActualTradeIndex(i);
            if (rawTradeIndex == -1) break;

            int rowY = y + 18 + i * 22;
            GuiRenderUtils.drawArrow(graphics, x + 54, rowY + 2);

            if (i < this.tradeButtons.size()) {
                this.tradeButtons.get(i).active = this.menu.isTradeExecutableByRawIndex(rawTradeIndex,this.minecraft.player);
            }
        }

        if (validTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            renderScrollbar(graphics, x + 156, y + 18, validTrades);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y, int totalTrades) {
        graphics.fill(x, y, x + 10, y + 86, 0xFF202020);

        int scrollbarHeight = 86;
        int thumbHeight = Math.max(12, (int) ((float) AutomatTradeMenu.VISIBLE_ROWS / totalTrades * scrollbarHeight));
        int thumbY = y + (int) (scrollController.getScrollAmount() * (scrollbarHeight - thumbHeight));

        graphics.fill(x, thumbY, x + 10, thumbY + thumbHeight, 0xFF8B8B8B);
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

        int totalTrades = this.menu.getValidTradesCount();
        if (totalTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            scrollController.onMouseScrolled(verticalAmount, totalTrades, AutomatTradeMenu.VISIBLE_ROWS);
            int newOffset = scrollController.getRowOffset(totalTrades, AutomatTradeMenu.VISIBLE_ROWS);
            updateScrollOffset(newOffset);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.menu.getBlockEntity() == null) return super.mouseDragged(event, dragX, dragY);

        int totalTrades = this.menu.getValidTradesCount();
        if (scrollController.isDragging() && totalTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            scrollController.onMouseDragged(event.y(), this.topPos + 18, 86);
            int newOffset = scrollController.getRowOffset(totalTrades, AutomatTradeMenu.VISIBLE_ROWS);
            updateScrollOffset(newOffset);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean button = event.button() == 0;
        int x = this.leftPos + 156;
        int y = this.topPos + 18;

        int validTrades = this.menu.getValidTradesCount();
        if (validTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            if (button && scrollController.onMouseClicked(event.x(), event.y(), x, y, 10, 86)) {
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
        return mouseX < guiLeft || mouseX >= guiLeft + MAIN_PANEL_WIDTH || mouseY < guiTop || mouseY >= guiTop + panelHeight;
    }
}