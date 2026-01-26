package pij.move;

import org.junit.jupiter.api.Test;
import pij.board.Direction;
import pij.board.Square;

import static org.junit.jupiter.api.Assertions.*;

public class MoveParserTest {

    private final MoveParser parser = new MoveParser();

    @Test
    void parsesPass() throws Exception {
        Move m = parser.parse(",", 7, 28);
        assertTrue(m.isPass());
    }

    @Test
    void parsesDownFormatLetterThenDigits() throws Exception {
        Move m = parser.parse("HI,f8", 26, 99);
        assertFalse(m.isPass());
        assertEquals("HI", m.wordRaw());
        assertEquals(Direction.DOWN, m.direction());
        assertEquals(new Square(7, 5), m.start()); // f8 -> col=5,row=7 (0-based)
    }

    @Test
    void parsesRightFormatDigitsThenLetter() throws Exception {
        Move m = parser.parse("HI,10b", 26, 99);
        assertEquals(Direction.RIGHT, m.direction());
        assertEquals(new Square(9, 1), m.start()); // 10b -> row=9,col=1
    }

    @Test
    void trimsSpaces() throws Exception {
        Move m = parser.parse("  Snow  ,   10b ", 26, 99);
        assertEquals("Snow", m.wordRaw());
        assertEquals(Direction.RIGHT, m.direction());
    }

    @Test
    void rejectsMissingComma() {
        assertThrows(MoveFormatException.class, () -> parser.parse("HI f8", 26, 99));
    }

    @Test
    void rejectsMultipleCommas() {
        assertThrows(MoveFormatException.class, () -> parser.parse("HI,f8,extra", 26, 99));
    }

    @Test
    void rejectsEmptyWord() {
        assertThrows(MoveFormatException.class, () -> parser.parse(",f8", 26, 99));
    }

    @Test
    void rejectsEmptySquare() {
        assertThrows(MoveFormatException.class, () -> parser.parse("HI,", 26, 99));
    }

    @Test
    void rejectsNonLettersInWord() {
        assertThrows(MoveFormatException.class, () -> parser.parse("H1,f8", 26, 99));
    }

    @Test
    void rejectsOutOfBoundsSquare() {
        // M=7 only columns a to g
        assertThrows(MoveFormatException.class, () -> parser.parse("HI,h1", 7, 28));
    }
}