package com.matthewp;

import com.matthewp.model.Cell;
import com.matthewp.model.Line;
import com.matthewp.model.Style;
import java.util.ArrayList;
import java.util.List;

public class TerminalBuffer {
    private int width;
    private int height;
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

    public void setStyle(Style style) {
        if (style != null) {
            this.currentStyle = style;
        } else {
            this.currentStyle = Style.DEFAULT;
        }
    }

    public void clear() {
        for (Line line : screen) {
            line.clear(currentStyle);
        }
        setCursor(0, 0);
    }

    public void fillLine(int y, char c) {
        if (y >= 0 && y < height) {
            screen.get(y).fill(c, currentStyle);
        }
    }

    public void clearAll() {
        clear();
        scrollback.clear();
    }

    public void write(String text) {
        if (text == null) return;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                newLine();
                continue;
            }

            Line currentLine = screen.get(cursorY);
            currentLine.setCell(cursorX, c, currentStyle);

            cursorX++;

            if (cursorX >= width) {
                newLine();
            }
        }
    }

    public void insert(String text) {
        if (text == null) return;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                newLine();
                continue;
            }

            Line currentLine = screen.get(cursorY);
            Cell pushed = currentLine.insertAt(cursorX, c, currentStyle);

            if (pushed != null && pushed.getContent() != ' ') {
                int nextY = cursorY + 1;
                if (nextY >= height) {
                    addEmptyLine();
                    cursorY--;
                    nextY = height - 1;
                }
                screen.get(nextY).insertAt(0, pushed.getContent(), pushed.getStyle());
            }

            cursorX++;

            if (cursorX >= width) {
                newLine();
            }
        }
    }

    private void newLine() {
        cursorX = 0;
        if (cursorY < height - 1) {
            cursorY++;
        } else {
            addEmptyLine();
        }
    }

    public void addEmptyLine() {
        Line top = screen.removeFirst();
        scrollback.addLast(top);

        if (scrollback.size() > maxScrollback) {
            scrollback.removeFirst();
        }

        screen.addLast(new Line(width));
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("Invalid dimensions");
        }

        while (screen.size() > newHeight) {
            scrollback.addLast(screen.removeFirst());
            if (scrollback.size() > maxScrollback) {
                scrollback.removeFirst();
            }
        }

        while (screen.size() < newHeight) {
            if (!scrollback.isEmpty()) {
                screen.addFirst(scrollback.removeLast().resized(newWidth));
            } else {
                screen.addLast(new Line(newWidth));
            }
        }

        if (newWidth != width) {
            for (int i = 0; i < screen.size(); i++) {
                screen.set(i, screen.get(i).resized(newWidth));
            }
        }

        this.width = newWidth;
        this.height = newHeight;
        setCursor(cursorX, cursorY);
    }

    public String getLineText(int y) {
        if (y < 0 || y >= height) {
            return "";
        }
        return screen.get(y).toString();
    }

    public int getScrollbackSize() {
        return scrollback.size();
    }

    public Line getLine(int y) {
        int scrollbackSize = scrollback.size();
        if (y < 0 || y >= scrollbackSize + height) {
            throw new IndexOutOfBoundsException("row " + y + " out of bounds");
        }
        if (y < scrollbackSize) {
            return scrollback.get(y);
        }
        return screen.get(y - scrollbackSize);
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < screen.size(); i++) {
            sb.append(screen.get(i).toString());
            if (i < screen.size() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String getFullContent() {
        StringBuilder sb = new StringBuilder();
        for (Line line : scrollback) {
            sb.append(line.toString());
            sb.append('\n');
        }
        for (int i = 0; i < screen.size(); i++) {
            sb.append(screen.get(i).toString());
            if (i < screen.size() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
