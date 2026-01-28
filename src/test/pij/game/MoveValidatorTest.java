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
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true, dict("HI")));
    }

    @Test
    void validFirstMovePlacesTilesAndConsumesFromRackCopyOnly() throws Exception {
        Board b = emptyBoard(7, 28, new Square(0,0)); // a1
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));

        Move m = Move.play("HI", new Square(0,0), pij.board.Direction.DOWN); // a1 downward
        ValidatedMove vm = new MoveValidator().validate(b, r, m, true, dict("HI"));

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
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true, dict("HI")));
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
        ValidatedMove vm = new MoveValidator().validate(b, r, m, true, dict("SNOW"));

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
        assertThrows(IllegalMoveException.class, () -> new MoveValidator().validate(b, r, m, true, dict("HI")));
    }

    private pij.dict.WordList dict(String... ws) {
        return new pij.dict.WordList(java.util.Set.of(ws));
    }

    @Test
    void rejectsConflictWithExistingTile() {
        Board b = emptyBoard(7, 28, new Square(0,0));
        // place 'H' at a1
        b.placeTile(0,0, Tile.normal('H', 4));

        Rack r = new Rack();
        r.add(Tile.normal('X', 8));
        r.add(Tile.normal('I', 1));

        Move m = Move.play("XI", new Square(0,0), pij.board.Direction.DOWN);
        assertThrows(IllegalMoveException.class, () ->
                new MoveValidator().validate(b, r, m, true, dict("HI", "XI"))
        );
    }

    @Test
    void rejectsWordNotInDictionary() {
        Board b = emptyBoard(7, 28, new Square(0,0));
        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));

        Move m = Move.play("HI", new Square(0,0), pij.board.Direction.DOWN);
        assertThrows(IllegalMoveException.class, () ->
                new MoveValidator().validate(b, r, m, true, dict("NOPE"))
        );
    }

    @Test
    void buildsMainWordIncludingExistingTiles() throws Exception {
        Board b = emptyBoard(7, 28, new Square(0,0));
        // existing 'N' 'O' at a2,a3 (down)
        b.placeTile(1,0, Tile.normal('N', 1));
        b.placeTile(2,0, Tile.normal('O', 1));

        Rack r = new Rack();
        r.add(Tile.normal('S', 1));
        r.add(Tile.normal('W', 4));

        // Input word must cover the whole path including existing tiles:
        // a1 S(new), a2 N(existing), a3 O(existing), a4 W(new)
        Move m = Move.play("SNOW", new Square(0,0), pij.board.Direction.DOWN);

        ValidatedMove vm = new MoveValidator().validate(b, r, m, true, dict("SNOW"));
        assertEquals("SNOW", vm.mainWord());
        assertEquals(2, vm.placements().size()); // only S and W are newly placed
    }

    @Test
    void rejectsIfPlacementCreatesPerpendicularWord() {
        Board b = emptyBoard(7, 28, new Square(0,0));

        // Put an existing tile below where we will place 'H' at a1:
        // If we place "HI" horizontally starting at a1, and there is an existing 'A' at a2,
        // then a1+a2 would form "HA" (length 2) as a perpendicular word -> illegal by spec.
        b.placeTile(1,0, Tile.normal('A', 1)); // a2

        Rack r = new Rack();
        r.add(Tile.normal('H', 4));
        r.add(Tile.normal('I', 1));

        Move m = Move.play("HI", new Square(0,0), pij.board.Direction.RIGHT);

        assertThrows(IllegalMoveException.class, () ->
                new MoveValidator().validate(b, r, m, true, dict("HI"))
        );
    }
}
