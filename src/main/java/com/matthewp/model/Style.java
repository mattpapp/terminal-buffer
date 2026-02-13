package com.matthewp.model;

public record Style(
        int foreground, int background, boolean bold, boolean italic, boolean underline) {
    public static final Style DEFAULT = new Style(-1, -1, false, false, false);
}
