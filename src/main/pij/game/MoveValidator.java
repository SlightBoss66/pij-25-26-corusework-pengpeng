package pij.game;

import pij.board.Board;
import pij.board.Direction;
import pij.board.Square;
import pij.move.Move;
import pij.tiles.Rack;
import pij.tiles.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MoveValidator {

    public ValidatedMove validate(Board board, Rack rack, Move move, boolean firstMove) throws IllegalMoveException {
        if (move.isPass()) {
            return new ValidatedMove("", List.of());
        }

        String word = move.wordRaw();
        if (word.length() < 2) {
            throw new IllegalMoveException("Word must have length >= 2");
        }

        Rack rackCopy = copyRack(rack);

        List<Placement> placements = planPlacements(board, rackCopy, word, move.start(), move.direction());

        if (placements.isEmpty()) {
            throw new IllegalMoveException("Move must place at least one tile");
        }

        if (firstMove) {
            boolean coversStart = placements.stream().anyMatch(p -> p.square().equals(board.startSquare()))
                    || coversStartViaExistingTiles(board, word, move.start(), move.direction());

            if (!coversStart) {
                throw new IllegalMoveException("First move must use start square");
            }
        }

        // mainWord will be constructed accurately in 10C; for now we return the raw word.
        return new ValidatedMove(word, placements);
    }

    private List<Placement> planPlacements(Board board, Rack rackCopy, String word, Square start, Direction dir)
            throws IllegalMoveException {

        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        int row = start.row();
        int col = start.col();

        List<Placement> placements = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            // advance to next empty cell (allow skipping occupied cells)
            while (board.inBounds(row, col) && !board.isEmptyAt(row, col)) {
                row += dr;
                col += dc;
            }
            if (!board.inBounds(row, col)) {
                throw new IllegalMoveException("Word does not fit on board");
            }

            char ch = word.charAt(i);

            Tile tileToPlace;
            if (Character.isUpperCase(ch)) {
                Optional<Tile> t = rackCopy.takeLetter(ch);
                if (t.isEmpty()) throw new IllegalMoveException("Missing tile: " + ch);
                tileToPlace = t.get();
            } else if (Character.isLowerCase(ch)) {
                Optional<Tile> w = rackCopy.takeWildcard();
                if (w.isEmpty()) throw new IllegalMoveException("Missing wildcard for: " + ch);
                Tile wildcard = w.get();
                wildcard.chooseLetter(ch);
                tileToPlace = wildcard;
            } else {
                throw new IllegalMoveException("Invalid character in word");
            }

            placements.add(new Placement(new Square(row, col), tileToPlace));

            row += dr;
            col += dc;
        }

        return placements;
    }

    private boolean coversStartViaExistingTiles(Board board, String word, Square start, Direction dir) {
        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        int row = start.row();
        int col = start.col();

        for (int i = 0; i < word.length(); i++) {
            while (board.inBounds(row, col) && !board.isEmptyAt(row, col)) {
                // existing tile occupies this square; it is part of the resulting word path
                if (row == board.startSquare().row() && col == board.startSquare().col()) return true;
                row += dr;
                col += dc;
            }
            if (!board.inBounds(row, col)) return false;

            if (row == board.startSquare().row() && col == board.startSquare().col()) return true;

            row += dr;
            col += dc;
        }
        return false;
    }

    private Rack copyRack(Rack original) {
        Rack r = new Rack();
        for (Tile t : original.tilesView()) {
            // Note: for now we assume tiles in rack are "fresh"; cloning is only needed to avoid removal.
            // We'll clone by creating new tiles with same properties.
            if (t.isWildcard()) {
                Tile w = Tile.wildcard();
                t.chosenLetter().ifPresent(w::chooseLetter);
                r.add(w);
            } else {
                r.add(Tile.normal(t.letter(), t.value()));
            }
        }
        return r;
    }
}
