package pij.game;

import org.junit.jupiter.api.Test;
import pij.board.Board;
import pij.board.Cell;
import pij.board.Square;
import pij.move.Move;
import pij.tiles.Rack;
import pij.tiles.Tile;

import static org.junit.jupiter.api.Assertions.*;

public class MoveValidatorTest {

    private Board emptyBoard(int m, int n, Square start) {
        Cell[][] cells = new Cell[n][m];
        for (int r = 0; r < n; r++) for (int c = 0; c < m; c++) cells[r][c] = Cell.normal();
        return new Board(m, n, start, cells);
    }

    @Test
    void firstMoveMustCoverStartSquare() {
        Board b = emptyBoard(7, 28, new Square(0,0)); // a1
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));
        Move m = Move.play("HI", new Square(1, 0), pij.board.Direction.DOWN); // starts at a2
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true));
    }

    @Test
    void validFirstMovePlacesTilesAndConsumesFromRackCopyOnly() throws Exception {
        Board b = emptyBoard(7, 28, new Square(0,0)); // a1
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));

        Move m = Move.play("HI", new Square(0,0), pij.board.Direction.DOWN); // a1 downward
        ValidatedMove vm = new MoveValidator().validate(b, r, m, true);

        assertEquals(2, vm.placements().size());
        // validate does not mutate original rack
        assertEquals(2, r.size());
    }

    @Test
    void rejectsIfMissingTile() {
        Board b = emptyBoard(7, 28, new Square(0,0));
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        // no I
        Move m = Move.play("HI", new Square(0,0), pij.board.Direction.DOWN);
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true));
    }

    @Test
    void lowercaseConsumesWildcard() throws Exception {
        Board b = emptyBoard(7, 28, new Square(0,0));
        Rack r = new Rack();
        r.add(Tile.normal('S', 1));
        r.add(Tile.wildcard()); // will become 'n'
        r.add(Tile.normal('O', 1));
        r.add(Tile.normal('W', 4));

        Move m = Move.play("SnOW", new Square(0,0), pij.board.Direction.RIGHT);
        ValidatedMove vm = new MoveValidator().validate(b, r, m, true);

        assertEquals(4, vm.placements().size());
        // ensure one placement is a wildcard bound to 'n'
        assertTrue(vm.placements().stream().anyMatch(p -> p.tile().isWildcard() && p.tile().displayChar() == 'n'));
    }

    @Test
    void rejectsIfWordFallsOffBoard() {
        Board b = emptyBoard(2, 10, new Square(0,0));
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));
        Move m = Move.play("HI", new Square(0,1), pij.board.Direction.RIGHT); // start at last col, can't fit 2
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true));
    }
}
