package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.client.gui.util.ScrollController;
import me.wuntare.tradeautomat.gui.AutomatTradeMenu;
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

    private static final int PADDING_LEFT = 14;
    private static final int ARROW_GAP = 12;
    private static final int BUTTON_GAP = 12;
    private static final int BUTTON_WIDTH = 38;
    private static final int SCROLLBAR_GAP = 10;
    private static final int SCROLLBAR_WIDTH = 14;
    private static final int PADDING_RIGHT = 8;

    private final ScrollController scrollController = new ScrollController();
    private final List<Button> tradeButtons = new ArrayList<>();

    public AutomatTradeScreen(AutomatTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    public int getDynamicPanelWidth() {
        int inputsWidth = this.menu.getActualInputSlots() * 18;
        int outputsWidth = this.menu.getActualOutputSlots() * 18;
        return PADDING_LEFT + inputsWidth + ARROW_GAP + outputsWidth + BUTTON_GAP + BUTTON_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH + PADDING_RIGHT;
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = getDynamicPanelWidth();
        int panelHeight = this.menu.getDynamicPanelHeight();

        this.leftPos = (this.width - panelWidth) / 2;
        this.topPos = (this.height - panelHeight) / 2;

        int visibleRows = this.menu.getVisibleRowsCount();
        int playerInvY = 18 + visibleRows * 22 + 10;

        this.inventoryLabelY = playerInvY - 11;
        this.titleLabelY = 6;

        int x = this.leftPos;
        int y = this.topPos;

        this.tradeButtons.clear();

        int inputsWidth = this.menu.getActualInputSlots() * 18;
        int outputsWidth = this.menu.getActualOutputSlots() * 18;
        int buttonX = x + PADDING_LEFT + inputsWidth + ARROW_GAP + outputsWidth + BUTTON_GAP;

        for (int i = 0; i < visibleRows; i++) {
            final int rowIndex = i;

            Button btn = Button.builder(Component.literal("Trade"), button -> {
                        int rawTradeIndex = this.menu.getActualTradeIndex(rowIndex);
                        if (rawTradeIndex != -1 && this.menu.getBlockEntity() != null) {
                            ClientPlayNetworking.send(new ExecuteTradePayload(this.menu.getBlockEntity().getBlockPos(), rawTradeIndex));
                        }
                    })
                    .bounds(buttonX, y + 18 + i * 22, BUTTON_WIDTH, 16)
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

        int panelWidth = getDynamicPanelWidth();
        int panelHeight = this.menu.getDynamicPanelHeight();

        GuiRenderUtils.drawMenu(graphics, x, y, panelWidth, panelHeight);

        GuiRenderUtils.drawMenuSlots(graphics, x, y, this.menu);

        if (this.menu.getBlockEntity() == null) return;

        int validTrades = this.menu.getValidTradesCount();
        int visibleRows = this.menu.getVisibleRowsCount();

        int inputsWidth = this.menu.getActualInputSlots() * 18;
        int outputsWidth = this.menu.getActualOutputSlots() * 18;

        int arrowX = x + PADDING_LEFT + inputsWidth + ARROW_GAP - 14;

        for (int i = 0; i < visibleRows; i++) {
            int rawTradeIndex = this.menu.getActualTradeIndex(i);
            Button btn = i < this.tradeButtons.size() ? this.tradeButtons.get(i) : null;

            if (rawTradeIndex != -1) {
                int rowY = y + 18 + i * 22;
                GuiRenderUtils.drawArrow(graphics, arrowX, rowY + 2);

                if (btn != null) {
                    btn.visible = true;
                    btn.active = this.minecraft != null
                            && this.minecraft.player != null
                            && this.menu.isTradeExecutableByRawIndex(rawTradeIndex, this.minecraft.player);
                }
            } else {
                if (btn != null) {
                    btn.visible = false;
                }
            }
        }

        if (validTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            int scrollbarX = x + PADDING_LEFT + inputsWidth + ARROW_GAP + outputsWidth + BUTTON_GAP + BUTTON_WIDTH + SCROLLBAR_GAP;
            renderScrollbar(graphics, scrollbarX, y + 18);
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
        int inputsWidth = this.menu.getActualInputSlots() * 18;
        int outputsWidth = this.menu.getActualOutputSlots() * 18;
        int scrollbarX = this.leftPos + PADDING_LEFT + inputsWidth + ARROW_GAP + outputsWidth + BUTTON_GAP + BUTTON_WIDTH + SCROLLBAR_GAP;
        int scrollbarY = this.topPos + 18;

        int validTrades = this.menu.getValidTradesCount();
        if (validTrades > AutomatTradeMenu.VISIBLE_ROWS) {
            if (button && scrollController.onMouseClicked(event.x(), event.y(), scrollbarX, scrollbarY, SCROLLBAR_WIDTH, 86)) {
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
        int panelWidth = getDynamicPanelWidth();
        int panelHeight = this.menu.getDynamicPanelHeight();
        return mouseX < guiLeft || mouseX >= guiLeft + panelWidth || mouseY < guiTop || mouseY >= guiTop + panelHeight;
    }
}