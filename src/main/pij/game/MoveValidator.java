package pij.game;

import pij.board.Board;
import pij.board.Direction;
import pij.board.Square;
import pij.dict.WordList;
import pij.move.Move;
import pij.tiles.Rack;
import pij.tiles.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MoveValidator {

    // Optional injected dictionary (so Main can call 4-arg validate)
    private final WordList injectedDict;

    public MoveValidator() {
        this.injectedDict = null;
    }

    public MoveValidator(WordList dict) {
        this.injectedDict = dict;
    }


    public ValidatedMove validate(Board board, Rack rack, Move move, boolean firstMove) throws IllegalMoveException {
        if (injectedDict == null) {
            throw new IllegalStateException(
                    "Dictionary not set. Construct MoveValidator(dict) or call validate(..., dict)."
            );
        }
        return validate(board, rack, move, firstMove, injectedDict);
    }

    public ValidatedMove validate(Board board, Rack rack, Move move, boolean firstMove, WordList dict)
            throws IllegalMoveException {

        if (move.isPass()) {
            return new ValidatedMove("", List.of());
        }

        String raw = move.wordRaw();
        if (raw == null || raw.length() < 2) {
            throw new IllegalMoveException("Word must have length >= 2");
        }

        Rack rackCopy = copyRack(rack);

        Planned planned = planWithExistingTiles(board, rackCopy, raw, move.start(), move.direction());

        if (planned.placements.isEmpty()) {
            throw new IllegalMoveException("Move must place at least one tile");
        }

        if (firstMove && !planned.pathSquares.contains(board.startSquare())) {
            throw new IllegalMoveException("First move must use start square");
        }

        if (createsPerpendicularWord(board, planned, move.direction())) {
            throw new IllegalMoveException("Move creates additional word(s)");
        }

        String mainWord = buildMainWord(board, planned, move.direction());

        if (!dict.contains(mainWord)) {
            throw new IllegalMoveException("Word not in dictionary");
        }

        return new ValidatedMove(mainWord, planned.placements);
    }

    private static final class Planned {
        final List<Placement> placements; // new tiles to place
        final List<Square> pathSquares;   // squares covered by raw input (length = raw.length)

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
            if (!board.inBounds(row, col)) {
                throw new IllegalMoveException("Word does not fit on board");
            }

            char ch = word.charAt(i);
            Square sq = new Square(row, col);
            path.add(sq);

            if (!board.isEmptyAt(row, col)) {
                // occupied -> must match
                char existing = normalizeBoardChar(board.tileAt(row, col));
                char expected = normalizeInputChar(ch);
                if (existing != expected) {
                    throw new IllegalMoveException("Conflicts with existing tile");
                }
            } else {
                // empty -> place tile from rack
                Tile t = takeTileForChar(rackCopy, ch);
                placements.add(new Placement(sq, t));
            }

            row += dr;
            col += dc;
        }

        return new Planned(placements, path);
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

    private int perpendicularWordLength(Board board, Planned planned, int row, int col, Direction dir) {
        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        // go to word start
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


    private String buildMainWord(Board board, Planned planned, Direction dir) {
        int dr = (dir == Direction.DOWN) ? 1 : 0;
        int dc = (dir == Direction.RIGHT) ? 1 : 0;

        Square anchor = planned.pathSquares.get(0);

        // move back to start
        int r = anchor.row();
        int c = anchor.col();
        while (board.inBounds(r - dr, c - dc) && hasTileAfterMove(board, planned, r - dr, c - dc)) {
            r -= dr;
            c -= dc;
        }

        // move forward building word
        StringBuilder sb = new StringBuilder();
        while (board.inBounds(r, c) && hasTileAfterMove(board, planned, r, c)) {
            sb.append(letterAtAfterMove(board, planned, r, c));
            r += dr;
            c += dc;
        }

        return sb.toString();
    }

    private boolean hasTileAfterMove(Board board, Planned planned, int row, int col) {
        if (!board.inBounds(row, col)) return false;
        if (!board.isEmptyAt(row, col)) return true;

        for (Placement p : planned.placements) {
            if (p.square().row() == row && p.square().col() == col) return true;
        }
        return false;
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

    private char normalizeInputChar(char ch) {
        if (Character.isUpperCase(ch)) return ch;
        if (Character.isLowerCase(ch)) return Character.toUpperCase(ch);
        throw new IllegalArgumentException("Invalid character: " + ch);
    }

    private char normalizeBoardChar(Tile t) {
        if (!t.isWildcard()) return t.letter();              // uppercase
        return Character.toUpperCase(t.displayChar());       // chosen letter is lowercase
    }

    private Tile takeTileForChar(Rack rackCopy, char ch) throws IllegalMoveException {
        if (Character.isUpperCase(ch)) {
            Optional<Tile> t = rackCopy.takeLetter(ch);
            if (t.isEmpty()) throw new IllegalMoveException("Missing tile: " + ch);
            return t.get();
        } else if (Character.isLowerCase(ch)) {
            Optional<Tile> w = rackCopy.takeWildcard();
            if (w.isEmpty()) throw new IllegalMoveException("Missing wildcard for: " + ch);
            Tile wildcard = w.get();
            wildcard.chooseLetter(ch);
            return wildcard;
        } else {
            throw new IllegalMoveException("Invalid character in word");
        }
    }

    private Rack copyRack(Rack original) {
        Rack r = new Rack();
        for (Tile t : original.tilesView()) {
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