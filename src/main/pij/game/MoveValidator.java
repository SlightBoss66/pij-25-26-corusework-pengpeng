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

    public ValidatedMove validate(Board board, Rack rack, Move move, boolean firstMove, pij.dict.WordList dict)
            throws IllegalMoveException {
        if (move.isPass()) return new ValidatedMove("", List.of());

        if (move.wordRaw().length() < 2) throw new IllegalMoveException("Word must have length >= 2");

        Rack rackCopy = copyRack(rack);

        Planned planned = planWithExistingTiles(board, rackCopy, move.wordRaw(), move.start(), move.direction());

        if (planned.placements.isEmpty()) throw new IllegalMoveException("Move must place at least one tile");

        if (firstMove && !planned.pathSquares.contains(board.startSquare())) {
            throw new IllegalMoveException("First move must use start square");
        }

        // One-word rule: no perpendicular words of length >= 2 created by new placements
        if (createsPerpendicularWord(board, planned, move.direction())) {
            throw new IllegalMoveException("Move creates additional word(s)");
        }

        // Dictionary check on main word (constructed from board + planned placements)
        String mainWord = buildMainWord(board, planned, move.direction());
        if (!dict.contains(mainWord)) {
            throw new IllegalMoveException("Word not in dictionary");
        }

        return new ValidatedMove(mainWord, planned.placements);
    }

    private static final class Planned {
        final List<Placement> placements;
        final List<Square> pathSquares; // squares covered by the word in order (including occupied squares)
        Planned(List<Placement> placements, List<Square> pathSquares) {
            this.placements = placements;
            this.pathSquares = pathSquares;
        }
    }

    private Planned planWithExistingTiles(Board board, Rack rackCopy, String word, Square start, Direction dir)
            throws IllegalMoveException {

        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        int row = start.row();
        int col = start.col();

        List<Placement> placements = new ArrayList<>();
        List<Square> path = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            if (!board.inBounds(row, col)) throw new IllegalMoveException("Word does not fit on board");

            char ch = word.charAt(i);
            Square sq = new Square(row, col);
            path.add(sq);

            if (!board.isEmptyAt(row, col)) {
                // Must match existing tile
                char existing = normalizeBoardChar(board.tileAt(row, col));
                char expected = normalizeInputChar(ch);
                if (existing != expected) {
                    throw new IllegalMoveException("Conflicts with existing tile");
                }
            } else {
                // Empty -> need to place a tile from rack
                Tile tileToPlace = takeTileForChar(rackCopy, ch);
                placements.add(new Placement(sq, tileToPlace));
            }

            row += dr;
            col += dc;
        }

        return new Planned(placements, path);
    }

    private char normalizeInputChar(char ch) {
        // Word input: uppercase means normal letter; lowercase means wildcard chosen letter.
        return Character.toUpperCase(ch);
    }

    private char normalizeBoardChar(pij.tiles.Tile t) {
        // Board tile might be wildcard with chosen letter -> compare as uppercase chosen letter
        if (!t.isWildcard()) return t.letter();
        return Character.toUpperCase(t.displayChar()); // chosen letter is lowercase
    }

    private Tile takeTileForChar(Rack rackCopy, char ch) throws IllegalMoveException {
        if (Character.isUpperCase(ch)) {
            return rackCopy.takeLetter(ch).orElseThrow(() -> new IllegalMoveException("Missing tile: " + ch));
        }
        if (Character.isLowerCase(ch)) {
            Tile w = rackCopy.takeWildcard().orElseThrow(() -> new IllegalMoveException("Missing wildcard for: " + ch));
            w.chooseLetter(ch);
            return w;
        }
        throw new IllegalMoveException("Invalid character in word");
    }
    private boolean createsPerpendicularWord(Board board, Planned planned, Direction mainDir) {
        Direction perp = (mainDir == Direction.RIGHT) ? Direction.DOWN : Direction.RIGHT;

        for (Placement p : planned.placements) {
            int row = p.square().row();
            int col = p.square().col();

            int len = perpendicularWordLength(board, planned, row, col, perp);
            if (len >= 2) return true;
        }
        return false;
    }

    private int perpendicularWordLength(Board board, Planned planned, int row, int col, Direction perp) {
        int dr = (perp == Direction.DOWN) ? 1 : 0;
        int dc = (perp == Direction.RIGHT) ? 1 : 0;

        // move to start of perpendicular word
        int r = row;
        int c = col;
        while (board.inBounds(r - dr, c - dc) && hasTileAfterMove(board, planned, r - dr, c - dc)) {
            r -= dr;
            c -= dc;
        }

        // count length
        int len = 0;
        while (board.inBounds(r, c) && hasTileAfterMove(board, planned, r, c)) {
            len++;
            r += dr;
            c += dc;
        }
        return len;
    }

    private boolean hasTileAfterMove(Board board, Planned planned, int row, int col) {
        if (!board.inBounds(row, col)) return false;
        if (!board.isEmptyAt(row, col)) return true;
        // If empty now, it will have a tile if it's one of the new placements
        for (Placement p : planned.placements) {
            if (p.square().row() == row && p.square().col() == col) return true;
        }
        return false;
    }

    private String buildMainWord(Board board, Planned planned, Direction dir) {
        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        // pick an anchor square on the word path (start)
        Square anchor = planned.pathSquares.get(0);

        // move to start (backwards) as long as there are tiles after move
        int r = anchor.row();
        int c = anchor.col();
        while (board.inBounds(r - dr, c - dc) && hasTileAfterMove(board, planned, r - dr, c - dc)) {
            r -= dr;
            c -= dc;
        }

        // walk forward building the word
        StringBuilder sb = new StringBuilder();
        while (board.inBounds(r, c) && hasTileAfterMove(board, planned, r, c)) {
            sb.append(letterAtAfterMove(board, planned, r, c));
            r += dr;
            c += dc;
        }
        return sb.toString();
    }

    private char letterAtAfterMove(Board board, Planned planned, int row, int col) {
        if (!board.isEmptyAt(row, col)) {
            return normalizeBoardChar(board.tileAt(row, col));
        }
        for (Placement p : planned.placements) {
            if (p.square().row() == row && p.square().col() == col) {
                return Character.toUpperCase(p.tile().displayChar());
            }
        }
        throw new IllegalStateException("No tile at " + row + "," + col);
    }
}
