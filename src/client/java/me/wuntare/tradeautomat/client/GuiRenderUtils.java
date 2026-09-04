package me.wuntare.tradeautomat.client;

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

            boolean isLocked = !slot.mayPlace(new ItemStack(net.minecraft.world.item.Items.DIRT));

            if (isLocked) {
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
            }
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