# Terminal Buffer

A terminal text buffer implementation in Java. This is the core data structure behind terminal emulators.

## What is this?

When a shell sends output, a terminal emulator needs somewhere to store and manipulate that text. This project implements that storage layer: a grid of character cells with styling, a cursor, a screen buffer, and a scrollback history.

It doesn't render anything, it's purely the data structure that a UI layer would read from.

## How to build and test

```
git clone https://github.com/mattpapp/terminal-buffer.git
cd terminal-buffer
mvn compile
mvn test
```

Requires Java 21 and Maven.

## Solution overview

At the lowest level there is `Cell` which is just a character and a `Style` (foreground color, background color, bold/italic/underline). A `Line` is a fixed-width array of these cells, basically a row in the terminal. `TerminalBuffer` then holds a list of lines that make up the screen (the visible area, eg, 80 columns x 24 rows) and a separate list for the scrollback (lines that scrolled off the top, saved for history).

The buffer keeps track of a cursor position and a current style. When you call `write("hello")`, it places each character into the cell at the cursor and moves the cursor forward. If the cursor reaches the end of a line it wraps down to the next one. If it's already on the last line, the top screen line gets pushed into the scrollback and a new blank line appears at the bottom, so everything shifts up by one.

`insert("X")` is different from `write`. Instead of overwriting what's already there, it shifts the existing characters on that line to the right to make room. If a character gets pushed off the right edge, it gets placed at the start of the next line below.

You can also set styles before writing so each character gets its own colors and formatting, move the cursor around (it stays within the screen bounds), fill or clear lines, clear the whole screen or screen+scrollback, and resize the terminal dimensions.

All content (both screen and scrollback) is accessible through `getLine(y)` which uses a single unified index where scrollback lines come first and screen lines come after. So if there are 3 scrollback lines and 24 screen lines, index 0-2 is scrollback and 3-26 is screen.

## Structure

Four classes:

- **`Style`** is an immutable record for foreground color, background color, bold/italic/underline. `-1` means default color.
- **`Cell`** holds one character and its style. Defaults to a space with default style.
- **`Line`** is a fixed-width array of cells. It can set, get, insert (shifting right), fill, clear, and resize.
- **`TerminalBuffer`** is the main class. It manages the screen (visible lines), scrollback (history), cursor, and all the editing operations.

## Design decisions

**Unified line access:** Instead of having separate `getChar(x, y)`, `getStyle(x, y)`, `getScrollbackLine(y)` methods on `TerminalBuffer`, I went with a single `getLine(y)` that uses a unified index (scrollback lines first, then screen lines). From there you call `getCell(x)` to get the character and style. The reason is to keep the API small and readable without losing functionality.

**Mutable cells, immutable style:** Cells get updated in place to avoid creating tons of objects when writing text. `Style` is a record so once created it doesn't change.

**Insert wraps one level deep:** When you `insert()` text and a character gets pushed off the end of a line it wraps to the next line. But if that next line is also full, the character pushed off *that* line is lost. Recursive wrapping across multiple lines would have been nicer (time permitting) and is something I would consider in a production setting.

**Trailing spaces aren't wrapped:** When inserting, if the character pushed off the end of a line is a plain space, it gets dropped instead of wrapped to the next line. Otherwise every insert on a non-full line would pointlessly cascade blank padding. The downside is that a space with a styled background (like a colored highlight) also gets dropped, but I am assuming this as a pretty rare edge case.

**Resize strategy:** Shrinking the height pushes top screen lines into scrollback and growing pulls them back. Width changes rebuild each line: truncating if narrower, padding if wider. I also made sure that lines restored from scrollback always get resized to the current width, even if the width didn't change in that specific resize call (because a previous resize might have left them with a stale width).

## What I'd improve with more time

- **Wide character support** (CJK, emoji etc.) where characters take 2 cells. Time did not allow me to try this but I would start with a `isWide` flag on Cell, placeholder cells for the right half and cursor movement that skips over them.
- **Scrollback line width on resize.** Right now scrollback lines keep whatever width they had when they were on screen. I could resize them too, but treating them as historical snapshots seems acceptable for the scope of this task.
- **`getLine()` returns mutable references**, including for scrollback lines which are supposed to be read-only. A defensive copy or read-only wrapper would be better.
- **Special character handling.** `\t` is written as a literal right now instead of expanding to the next tab stop. Same for `\r` (carriage return), `\b` (backspace), escape sequences etc. Given more time I would handle at least tabs and backspaces.
- **Recursive insert wrapping** so pushed characters cascade across multiple full lines instead of being lost
- **More test edge cases** like inserting at every position of a full line, writing exactly `width` characters repeatedly, resize down to 1x1 then back up etc.
