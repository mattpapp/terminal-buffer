package com.matthewp.model;

public class Cell {
    private char content;
    private Style style;

    public Cell() {
        this.content = ' ';
        this.style = new Style(-1, -1, false, false, false);
    }

    public void update(char content, Style style) {
        this.content = content;
        this.style = style;
    }

    public char getContent() {
        return content;
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public String toString() {
        return String.valueOf(content);
    }
}
