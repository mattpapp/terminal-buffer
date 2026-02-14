package com.matthewp;

import static org.junit.jupiter.api.Assertions.*;

import com.matthewp.model.Style;
import org.junit.jupiter.api.Test;

class TerminalBufferTest {

    @Test
    void testBasicWrite() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 10);
        buffer.write("Hoi");
        assertEquals(3, buffer.getCursorX());
        assertEquals(0, buffer.getCursorY());
    }

    @Test
    void testNewline() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 10);
        buffer.write("first\nsecond");
        assertEquals(6, buffer.getCursorX());
        assertEquals(1, buffer.getCursorY());
    }

    @Test
    void testAutoWrap() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 10);
        buffer.write("1234567890abc");
        assertEquals(3, buffer.getCursorX());
        assertEquals(1, buffer.getCursorY());
    }

    @Test
    void testClear() {
        TerminalBuffer buffer = new TerminalBuffer(10, 2, 0);
        buffer.write("Hoi");
        buffer.setCursor(5, 1);

        buffer.clear();

        assertEquals(0, buffer.getCursorX());
        assertEquals(0, buffer.getCursorY());
        assertEquals("          ", buffer.getLineText(0));
    }

    @Test
    void testFillLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 1, 0);
        buffer.fillLine(0, 'x');
        assertEquals("xxxxx", buffer.getLineText(0));
    }

    @Test
    void testCursorClamping() {
        TerminalBuffer buffer = new TerminalBuffer(10, 10, 0);
        buffer.setCursor(-10, 50);

        assertEquals(0, buffer.getCursorX());
        assertEquals(9, buffer.getCursorY());
    }

    @Test
    void testWriteEmptyOrNull() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 10);
        buffer.setCursor(5, 5);

        buffer.write(null);
        buffer.write("");

        assertEquals(5, buffer.getCursorX());
        assertEquals(5, buffer.getCursorY());
    }

    @Test
    void testBottomRightCornerWrap() {
        TerminalBuffer buffer = new TerminalBuffer(10, 2, 5);
        buffer.setCursor(9, 1);

        buffer.write("A");

        assertEquals(0, buffer.getCursorX());
        assertEquals(1, buffer.getCursorY());
    }

    @Test
    void testGetCell() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.write("Hi");
        assertEquals('H', buffer.getLine(0).getCell(0).getContent());
        assertEquals(' ', buffer.getLine(0).getCell(5).getContent());
    }

    @Test
    void testGetCellStyle() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        Style custom = new Style(1, 2, true, false, false);
        buffer.setStyle(custom);
        buffer.write("a");

        assertEquals(custom, buffer.getLine(0).getCell(0).getStyle());
    }

    @Test
    void testGetLineUnified() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.write("aaaa\nbbbb\ncccc");

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("aaaa ", buffer.getLine(0).toString());
        assertEquals("bbbb ", buffer.getLine(1).toString());
        assertEquals("cccc ", buffer.getLine(2).toString());
    }

    @Test
    void testScrollbackEviction() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 3);
        for (int i = 0; i < 10; i++) {
            buffer.write("line\n");
        }
        assertTrue(buffer.getScrollbackSize() <= 3);
    }

    @Test
    void testInsertEmptyLine() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.write("hello");
        buffer.setCursor(3, 1);

        buffer.addEmptyLine();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("hello", buffer.getLine(0).toString());
        assertEquals("     ", buffer.getLineText(1));
        assertEquals(3, buffer.getCursorX());
        assertEquals(1, buffer.getCursorY());
    }

    @Test
    void testInsertShiftsRight() {
        TerminalBuffer buffer = new TerminalBuffer(10, 2, 0);
        buffer.write("abcde");
        buffer.setCursor(2, 0);

        buffer.insert("X");

        assertEquals("abXcde    ", buffer.getLineText(0));
        assertEquals(3, buffer.getCursorX());
    }

    @Test
    void testInsertWraps() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 0);
        buffer.write("abcde");
        buffer.setCursor(0, 0);

        buffer.insert("X");

        assertEquals("Xabcd", buffer.getLineText(0));
        assertEquals("e    ", buffer.getLineText(1));
    }

    @Test
    void testInsertAtBottomLineScrolls() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.fillLine(0, 'a');
        buffer.fillLine(1, 'b');
        buffer.setCursor(0, 1);

        buffer.insert("X");

        assertEquals("Xbbbb", buffer.getLineText(0));
        assertEquals(0, buffer.getCursorY());
        assertEquals(1, buffer.getCursorX());
    }

    @Test
    void testMoveLeft() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.write("hello");
        buffer.moveLeft(3);

        assertEquals(2, buffer.getCursorX());
        assertEquals(0, buffer.getCursorY());
    }

    @Test
    void testMoveRight() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.moveRight(4);

        assertEquals(4, buffer.getCursorX());
    }

    @Test
    void testMoveDown() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.moveDown(3);

        assertEquals(3, buffer.getCursorY());
    }

    @Test
    void testMoveUp() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.setCursor(0, 4);
        buffer.moveUp(2);

        assertEquals(2, buffer.getCursorY());
    }

    @Test
    void testMoveStopsAtEdge() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 0);
        buffer.moveLeft(100);
        assertEquals(0, buffer.getCursorX());

        buffer.moveUp(100);
        assertEquals(0, buffer.getCursorY());

        buffer.moveRight(100);
        assertEquals(9, buffer.getCursorX());

        buffer.moveDown(100);
        assertEquals(4, buffer.getCursorY());
    }

    @Test
    void testClearAll() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.write("aaa\nbbb\nccc");

        buffer.clearAll();

        assertEquals(0, buffer.getCursorX());
        assertEquals(0, buffer.getCursorY());
        assertEquals(0, buffer.getScrollbackSize());
        assertEquals("     ", buffer.getLineText(0));
    }

    @Test
    void testGetScreenAsString() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 0);
        buffer.write("hi");
        buffer.setCursor(0, 1);
        buffer.write("bye");

        String screen = buffer.getScreenAsString();
        assertEquals("hi   \nbye  ", screen);
    }

    @Test
    void testDimensions() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 100);
        assertEquals(80, buffer.getWidth());
        assertEquals(24, buffer.getHeight());
    }

    @Test
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 24, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(80, 24, -1));
    }

    @Test
    void testGetFullContent() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.write("aaaa\nbbbb\ncccc");

        String full = buffer.getFullContent();
        assertTrue(full.contains("aaaa"));
        assertTrue(full.contains("cccc"));
    }

    @Test
    void testResizeShrinkHeight() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 10);
        buffer.write("aaa\nbbb\nccc");

        buffer.resize(5, 2);

        assertEquals(2, buffer.getHeight());
        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("bbb  ", buffer.getLineText(0));
        assertEquals("ccc  ", buffer.getLineText(1));
    }

    @Test
    void testResizeGrowHeightRestoresScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.write("aaa\nbbb\nccc");

        assertEquals(1, buffer.getScrollbackSize());

        buffer.resize(5, 3);

        assertEquals(3, buffer.getHeight());
        assertEquals(0, buffer.getScrollbackSize());
        assertEquals("aaa  ", buffer.getLineText(0));
    }

    @Test
    void testResizeChangesWidth() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 0);
        buffer.write("abcde");

        buffer.resize(3, 2);

        assertEquals(3, buffer.getWidth());
        assertEquals("abc", buffer.getLineText(0));
    }

    @Test
    void testResizeClampsCursor() {
        TerminalBuffer buffer = new TerminalBuffer(10, 10, 0);
        buffer.setCursor(8, 7);

        buffer.resize(5, 3);

        assertEquals(4, buffer.getCursorX());
        assertEquals(2, buffer.getCursorY());
    }
}
