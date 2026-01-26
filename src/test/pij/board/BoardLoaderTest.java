package pij.board;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class BoardLoaderTest {

    @Test
    void loadsValidBoard() throws Exception {
        String text = ""
                + "7\n"     // M
                + "28\n"    // N  (7*28=196 >= 192)
                + "a1\n"    // start square
                + rows(28, 7, "."); // 28 lines, each line 7 tokens
        Board b = new BoardLoader().load(new StringReader(text));
        assertEquals(7, b.cols());
        assertEquals(28, b.rows());
        assertEquals(new Square(0, 0), b.startSquare());
        assertEquals(CellType.NORMAL, b.cellAt(0,0).type());
    }

    @Test
    void rejectsMTooSmall() {
        String text = "6\n28\na1\n" + rows(28, 6, ".");
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    @Test
    void rejectsNTooSmall() {
        String text = "7\n9\na1\n" + rows(9, 7, ".");
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    @Test
    void rejectsAreaTooSmall() {
        String text = "7\n27\na1\n" + rows(27, 7, ".");
        // 7*27 = 189 < 192
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    @Test
    void parsesPremiumTokens() throws Exception {
        String text = ""
                + "7\n28\na1\n"
                + "[2] <3> . . . . .\n"
                + rows(27, 7, ".");
        Board b = new BoardLoader().load(new StringReader(text));
        assertEquals(CellType.LETTER_PREMIUM, b.cellAt(0,0).type());
        assertEquals(2, b.cellAt(0,0).factor());
        assertEquals(CellType.WORD_PREMIUM, b.cellAt(0,1).type());
        assertEquals(3, b.cellAt(0,1).factor());
    }

    @Test
    void rejectsBadToken() {
        String text = ""
                + "7\n28\na1\n"
                + "X . . . . . .\n"
                + rows(27, 7, ".");
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    @Test
    void rejectsWrongTokenCountInRow() {
        String text = ""
                + "7\n28\na1\n"
                + ". . . . . .\n" // only 6 tokens
                + rows(27, 7, ".");
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    @Test
    void rejectsStartSquareOutOfBounds() {
        String text = ""
                + "7\n28\nh1\n" // col h => 8th column, out of bounds for M=7
                + rows(28, 7, ".");
        assertThrows(InvalidBoardFileException.class, () -> new BoardLoader().load(new StringReader(text)));
    }

    private static String rows(int nRows, int mCols, String token) {
        StringBuilder sb = new StringBuilder();
        String line = String.join(" ", java.util.Collections.nCopies(mCols, token)) + "\n";
        for (int i = 0; i < nRows; i++) sb.append(line);
        return sb.toString();
    }
}