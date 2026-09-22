package me.wuntare.tradeautomat.client.gui.util;

public class ScrollController {
    private float scrollAmount = 0.0F;
    private boolean isDragging = false;

    public void onMouseScrolled(double delta, int totalRows, int visibleRows) {
        if (totalRows <= visibleRows) return;
        int hiddenRows = totalRows - visibleRows;
        this.scrollAmount = Math.clamp(this.scrollAmount - (float) (delta / hiddenRows), 0.0F, 1.0F);
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int x, int y, int width, int height) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            this.isDragging = true;
            return true;
        }
        return false;
    }

    public void onMouseReleased() {
        this.isDragging = false;
    }

    public void onMouseDragged(double mouseY, int startY, int trackHeight) {
        if (this.isDragging) {
            this.scrollAmount = Math.clamp((float) (mouseY - startY) / trackHeight, 0.0F, 1.0F);
        }
    }

    public int getRowOffset(int totalRows, int visibleRows) {
        if (totalRows <= visibleRows) return 0;
        int hiddenRows = totalRows - visibleRows;
        return (int) ((double) (this.scrollAmount * hiddenRows) + 0.5D);
    }

    public float getScrollAmount() {
        return scrollAmount;
    }

    public boolean isDragging() {
        return isDragging;
    }
}