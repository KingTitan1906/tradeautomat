package me.wuntare.tradeautomat.client;

import me.wuntare.tradeautomat.gui.AutomatStorageMenu;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class GuiRenderUtils {
    public static final int ATLAS_TILE_SIZE = 18;
    public static final int MIN_MENU_SIZE = ATLAS_TILE_SIZE * 2;

    private static final int ATLAS_WIDTH = 162;
    private static final int ATLAS_HEIGHT = 36;
    private static final Identifier GUI_ATLAS = Identifier.fromNamespaceAndPath(
            "tradeautomat",
            "textures/gui/hud-atlas.png"
    );
    private static final Identifier GUI_ARROW = Identifier.fromNamespaceAndPath(
            "tradeautomat",
            "textures/gui/arrow.png"
    );
    private static final Identifier SCROLLER_BOX = Identifier.fromNamespaceAndPath(
            "tradeautomat",
            "textures/gui/scroller_box.png"
    );
    private static final Identifier SCROLLER_TILE = Identifier.fromNamespaceAndPath(
            "tradeautomat",
            "textures/gui/scroller_tile.png"
    );

    private GuiRenderUtils() {}

    public static void drawMenu(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height
    ) {
        width = Math.max(MIN_MENU_SIZE, width);
        height = Math.max(MIN_MENU_SIZE, height);

        int middleWidth = width - ATLAS_TILE_SIZE * 2;
        int middleHeight = height - ATLAS_TILE_SIZE * 2;

        drawPart(graphics, x, y, 0, 0, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE);
        drawPart(graphics, x + width - ATLAS_TILE_SIZE, y, 18, 0, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE);
        drawPart(graphics, x, y + height - ATLAS_TILE_SIZE, 36, 0, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE);
        drawPart(graphics, x + width - ATLAS_TILE_SIZE, y + height - ATLAS_TILE_SIZE, 54, 0, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE);

        drawPart(graphics, x + ATLAS_TILE_SIZE, y, 72, 0, middleWidth, ATLAS_TILE_SIZE);
        drawPart(graphics, x, y + ATLAS_TILE_SIZE, 90, 0, ATLAS_TILE_SIZE, middleHeight);
        drawPart(graphics, x + ATLAS_TILE_SIZE, y + height - ATLAS_TILE_SIZE, 108, 0, middleWidth, ATLAS_TILE_SIZE);
        drawPart(graphics, x + width - ATLAS_TILE_SIZE, y + ATLAS_TILE_SIZE, 126, 0, ATLAS_TILE_SIZE, middleHeight);
        drawPart(graphics, x + ATLAS_TILE_SIZE, y + ATLAS_TILE_SIZE, 144, 0, middleWidth, middleHeight);
    }

    public static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        drawPart(graphics, x - 1, y - 1, 0, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE, ATLAS_TILE_SIZE);
    }

    public static void drawMenuSlots(GuiGraphicsExtractor graphics, int leftPos, int topPos, AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;

            drawSlot(graphics, slotX, slotY);
        }
    }

    public static void drawTradeSetupMenuSlots(GuiGraphicsExtractor graphics, int x, int y, AutomatTradeSetupMenu menu) {
        for (Slot slot : menu.slots) {
            int slotX = x + slot.x;
            int slotY = y + slot.y;

            drawSlot(graphics, slotX, slotY);
            boolean isLocked = false;
            if (slot instanceof AutomatTradeSetupMenu.TradeSetupGhostSlot tradeSlot) {
                isLocked = tradeSlot.isLocked();
            }

            if (isLocked) graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
        }
    }
    public static void drawStorageMenuSlots(GuiGraphicsExtractor graphics, int x, int y, AutomatStorageMenu menu) {
        for (Slot slot : menu.slots) {
            int slotX = x + slot.x;
            int slotY = y + slot.y;

            drawSlot(graphics, slotX, slotY);
            boolean isLocked = !slot.mayPlace(ItemStack.EMPTY);

            if (isLocked) graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
        }
    }

    public static void drawSlotGrid(GuiGraphicsExtractor graphics, int x, int y, int columns, int rows) {
        drawSlotGrid(graphics, x, y, columns, rows, columns * rows);
    }

    public static void drawSlotGrid(GuiGraphicsExtractor graphics, int x, int y, int columns, int rows, int maxUnlocked) {
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (index >= maxUnlocked) return;
                drawSlot(graphics, x + c * ATLAS_TILE_SIZE, y + r * ATLAS_TILE_SIZE);
                index++;
            }
        }
    }

    public static void drawArrow(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_ARROW, x, y, 0, 0, 16, 13, 16, 13, 16, 13);
    }

    public static void drawScrollbar(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int totalHeight,
            float scrollProgress
    ) {
        draw9SliceBox(graphics, SCROLLER_BOX, x, y, width, totalHeight, 1, 3, 3, true);

        int tileHeight = 15;
        int padding = 1;

        int maxScrollDistance = (totalHeight - padding * 2) - tileHeight;

        int thumbY = y + padding + Math.round(scrollProgress * maxScrollDistance);

        drawTexturePart(
                graphics,
                SCROLLER_TILE,
                x + 1, thumbY,
                0, 0,
                12, tileHeight,
                12, tileHeight,
                12, tileHeight
        );
    }

    public static void draw9SliceBox(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x, int y,
            int width, int height,
            int cornerSize,
            int texW, int texH,
            boolean inverted
    ) {
        int midW = width - cornerSize * 2;
        int midH = height - cornerSize * 2;

        int texMidW = texW - cornerSize * 2;
        int texMidH = texH - cornerSize * 2;

        int uLeft   = inverted ? texW - cornerSize : 0;
        int uRight  = inverted ? 0 : texW - cornerSize;
        int uCenter = cornerSize;

        int vTop    = inverted ? texH - cornerSize : 0;
        int vBottom = inverted ? 0 : texH - cornerSize;
        int vCenter = cornerSize;

        int uCenterFill = inverted ? (texW - cornerSize) : cornerSize;
        int vCenterFill = inverted ? 0 : cornerSize;

        drawTexturePart(graphics, texture, x, y, uLeft, vTop, cornerSize, cornerSize, cornerSize, cornerSize, texW, texH);
        drawTexturePart(graphics, texture, x + width - cornerSize, y, uRight, vTop, cornerSize, cornerSize, cornerSize, cornerSize, texW, texH);
        drawTexturePart(graphics, texture, x, y + height - cornerSize, uLeft, vBottom, cornerSize, cornerSize, cornerSize, cornerSize, texW, texH);
        drawTexturePart(graphics, texture, x + width - cornerSize, y + height - cornerSize, uRight, vBottom, cornerSize, cornerSize, cornerSize, cornerSize, texW, texH);

        drawTexturePart(graphics, texture, x + cornerSize, y, uCenter, vTop, midW, cornerSize, texMidW, cornerSize, texW, texH);
        drawTexturePart(graphics, texture, x + cornerSize, y + height - cornerSize, uCenter, vBottom, midW, cornerSize, texMidW, cornerSize, texW, texH);
        drawTexturePart(graphics, texture, x, y + cornerSize, uLeft, vCenter, cornerSize, midH, cornerSize, texMidH, texW, texH);
        drawTexturePart(graphics, texture, x + width - cornerSize, y + cornerSize, uRight, vCenter, cornerSize, midH, cornerSize, texMidH, texW, texH);

        drawTexturePart(graphics, texture, x + cornerSize, y + cornerSize, uCenterFill, vCenterFill, midW, midH, texMidW, texMidH, texW, texH);
    }
    private static void drawTexturePart(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int destX, int destY,
            int u, int v,
            int destW, int destH,
            int srcW, int srcH,
            int texW, int texH
    ) {
        if (destW <= 0 || destH <= 0) return;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                destX, destY,
                u, v,
                destW, destH,
                srcW, srcH,
                texW, texH
        );
    }
    private static void drawPart(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int textureX,
            int textureY,
            int width,
            int height
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GUI_ATLAS,
                x,
                y,
                textureX,
                textureY,
                width,
                height,
                ATLAS_TILE_SIZE,
                ATLAS_TILE_SIZE,
                ATLAS_WIDTH,
                ATLAS_HEIGHT
        );
    }
}