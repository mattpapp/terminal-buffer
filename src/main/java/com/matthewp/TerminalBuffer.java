package com.matthewp;

import com.matthewp.model.Line;
import com.matthewp.model.Style;
import java.util.ArrayList;
import java.util.List;

public class TerminalBuffer {
    private final int width;
    private final int height;
    private final int maxScrollback;

    private final List<Line> screen;
    private final List<Line> scrollback;

    private int cursorX = 0;
    private int cursorY = 0;

    private Style currentStyle = Style.DEFAULT;

    public TerminalBuffer(int width, int height, int maxScrollback) {
        if (width <= 0 || height <= 0 || maxScrollback < 0) {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        this.width = width;
        this.height = height;
        this.maxScrollback = maxScrollback;

        this.screen = new ArrayList<>(height);
        this.scrollback = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            screen.add(new Line(width));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public void setCursor(int x, int y) {
        this.cursorX = Math.max(0, Math.min(x, width - 1));
        this.cursorY = Math.max(0, Math.min(y, height - 1));
    }

    public void moveCursor(int dx, int dy) {
        setCursor(cursorX + dx, cursorY + dy);
    }

    public void moveUp(int n) {
        moveCursor(0, -n);
    }

    public void moveDown(int n) {
        moveCursor(0, n);
    }

    public void moveLeft(int n) {
        moveCursor(-n, 0);
    }

    public void moveRight(int n) {
        moveCursor(n, 0);
    }

    public void write(String text) {
        if (text == null) return;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                cursorX = 0;
                if (cursorY < height - 1) {
                    cursorY++;
                } else {
                    Line removed = screen.removeFirst();
                    scrollback.addLast(removed);
                    if (scrollback.size() > maxScrollback) {
                        scrollback.removeFirst();
                    }
                    screen.addLast(new Line(width));
                }
                continue;
            }

            screen.get(cursorY).setCell(cursorX, c, currentStyle);
            cursorX++;

            if (cursorX >= width) {
                cursorX = 0;
                if (cursorY < height - 1) {
                    cursorY++;
                } else {
                    Line removed = screen.removeFirst();
                    scrollback.addLast(removed);
                    if (scrollback.size() > maxScrollback) {
                        scrollback.removeFirst();
                    }
                    screen.addLast(new Line(width));
                }
            }
        }
    }
}
