package com.matthewp.model;

public class Cell {
    private char content;
    private Style style;

    public Cell() {
        this.content = ' ';
        this.style = Style.DEFAULT;
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

}
