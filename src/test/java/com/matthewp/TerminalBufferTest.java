package com.matthewp;

import static org.junit.jupiter.api.Assertions.*;

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
}
