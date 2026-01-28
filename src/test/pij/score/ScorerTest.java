package pij.score;

import org.junit.jupiter.api.Test;
import pij.board.Board;
import pij.board.Cell;
import pij.board.Square;
import pij.game.MoveApplier;
import pij.game.Placement;
import pij.game.ValidatedMove;
import pij.tiles.Tile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScorerTest {

    private Board boardWithAllNormal(int m, int n, Square start) {
        Cell[][] cells = new Cell[n][m];
        for (int r = 0; r < n; r++) for (int c = 0; c < m; c++) cells[r][c] = Cell.normal();
        return new Board(m, n, start, cells);
    }

    @Test
    void passScoresZero() {
        Board b = boardWithAllNormal(7, 28, new Square(0,0));
        ScoreBreakdown sb = new Scorer().scoreMove(b, new ValidatedMove("", List.of()));
        assertEquals(0, sb.total());
    }

    @Test
    void normalWordScoresSumOfTiles() {
        Board b = boardWithAllNormal(7, 28, new Square(0,0));
        ValidatedMove vm = new ValidatedMove("HI", List.of(
                new Placement(new Square(0,0), Tile.normal('H', 4)),
                new Placement(new Square(0,1), Tile.normal('I', 1))
        ));
        new MoveApplier().apply(b, new pij.tiles.Rack(), vm); // rack not needed for scoring here
        ScoreBreakdown sb = new Scorer().scoreMove(b, vm);
        assertEquals(5, sb.total());
    }

    @Test
    void letterPremiumMultipliesOnlyNewlyPlacedTile() {
        Board b = boardWithAllNormal(7, 28, new Square(0,0));
        // Make a1 a letter premium [2]
        b.cellAt(0,0); // ensure exists
        // easiest: replace cell object
        // We'll rebuild cells to set premium:
        Cell[][] cells = new Cell[28][7];
        for (int r = 0; r < 28; r++) for (int c = 0; c < 7; c++) cells[r][c] = Cell.normal();
        cells[0][0] = Cell.letter(2);
        b = new Board(7, 28, new Square(0,0), cells);

        ValidatedMove vm = new ValidatedMove("HI", List.of(
                new Placement(new Square(0,0), Tile.normal('H', 4)), // on [2]
                new Placement(new Square(0,1), Tile.normal('I', 1))
        ));
        new MoveApplier().apply(b, new pij.tiles.Rack(), vm);

        ScoreBreakdown sb = new Scorer().scoreMove(b, vm);
        // H: 4*2=8, I:1 => total 9
        assertEquals(9, sb.total());
        assertEquals(9, sb.baseBeforeWordMultiplier());
        assertEquals(1, sb.wordMultiplier());
    }

    @Test
    void wordPremiumMultipliesWholeWord() {
        Cell[][] cells = new Cell[28][7];
        for (int r = 0; r < 28; r++) for (int c = 0; c < 7; c++) cells[r][c] = Cell.normal();
        cells[0][1] = Cell.word(3); // <3> on b1

        Board b = new Board(7, 28, new Square(0,0), cells);

        ValidatedMove vm = new ValidatedMove("HI", List.of(
                new Placement(new Square(0,0), Tile.normal('H', 4)),
                new Placement(new Square(0,1), Tile.normal('I', 1)) // on <3>
        ));
        new MoveApplier().apply(b, new pij.tiles.Rack(), vm);

        ScoreBreakdown sb = new Scorer().scoreMove(b, vm);
        // base 5, word multiplier 3 => 15
        assertEquals(15, sb.total());
        assertEquals(3, sb.wordMultiplier());
    }

    @Test
    void negativeAndZeroPremiumsWork() {
        Cell[][] cells = new Cell[28][7];
        for (int r = 0; r < 28; r++) for (int c = 0; c < 7; c++) cells[r][c] = Cell.normal();
        cells[0][0] = Cell.letter(0); // [0]
        cells[0][1] = Cell.word(-2);  // <-2>

        Board b = new Board(7, 28, new Square(0,0), cells);

        ValidatedMove vm = new ValidatedMove("HI", List.of(
                new Placement(new Square(0,0), Tile.normal('H', 4)), // 4*0 = 0
                new Placement(new Square(0,1), Tile.normal('I', 1))  // word * (-2)
        ));
        new MoveApplier().apply(b, new pij.tiles.Rack(), vm);

        ScoreBreakdown sb = new Scorer().scoreMove(b, vm);
        // base: H=0, I=1 => 1; word multiplier -2 => -2
        assertEquals(-2, sb.total());
    }

    @Test
    void bingoAddsSixtyAfterMultipliers() {
        Board b = boardWithAllNormal(15, 15, new Square(7,7));
        ValidatedMove vm = new ValidatedMove("ABCDEFG", List.of(
                new Placement(new Square(7,7), Tile.normal('A', 1)),
                new Placement(new Square(7,8), Tile.normal('B', 3)),
                new Placement(new Square(7,9), Tile.normal('C', 3)),
                new Placement(new Square(7,10), Tile.normal('D', 2)),
                new Placement(new Square(7,11), Tile.normal('E', 1)),
                new Placement(new Square(7,12), Tile.normal('F', 4)),
                new Placement(new Square(7,13), Tile.normal('G', 2))
        ));
        new MoveApplier().apply(b, new pij.tiles.Rack(), vm);

        ScoreBreakdown sb = new Scorer().scoreMove(b, vm);
        assertEquals(sb.baseBeforeWordMultiplier() * sb.wordMultiplier() + 60, sb.total());
        assertEquals(60, sb.bingoBonus());
    }
}
