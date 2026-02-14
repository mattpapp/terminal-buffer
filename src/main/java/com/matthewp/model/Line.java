package com.matthewp.model;

public class Line {
    private final Cell[] cells;

    public Line(int width) {
        if (width <= 0) throw new IllegalArgumentException("Width must be > 0");

        this.cells = new Cell[width];

        for (int i = 0; i < width; i++) {
            cells[i] = new Cell();
        }
    }

    public void setCell(int x, char c, Style style) {
        if (x < 0 || x >= cells.length) return;
        cells[x].update(c, style);
    }

    public Cell getCell(int x) {
        if (x < 0 || x >= cells.length) {
            throw new IndexOutOfBoundsException("Column " + x + " is out of bounds");
        }
        return cells[x];
    }

    public Cell insertAt(int x, char c, Style style) {
        if (x < 0 || x >= cells.length) return null;

        Cell last = cells[cells.length - 1];
        char lastChar = last.getContent();
        Style lastStyle = last.getStyle();

        for (int i = cells.length - 1; i > x; i--) {
            cells[i].update(cells[i - 1].getContent(), cells[i - 1].getStyle());
        }
        cells[x].update(c, style);

        Cell pushed = new Cell();
        pushed.update(lastChar, lastStyle);
        return pushed;
    }

    public void fill(char c, Style style) {
        for (Cell cell : cells) {
            cell.update(c, style);
        }
    }

    public void clear(Style style) {
        fill(' ', style);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (Cell cell : cells) {
            sb.append(cell.getContent());
        }
        return sb.toString();
    }
}
