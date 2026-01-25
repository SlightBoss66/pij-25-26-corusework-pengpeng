package pij.move;

import pij.board.Direction;
import pij.board.Square;

public final class Move {
    private final boolean pass;
    private final String wordRaw;       // keep original case
    private final Square start;
    private final Direction direction;  // RIGHT or DOWN

    private Move(boolean pass, String wordRaw, Square start, Direction direction) {
        this.pass = pass;
        this.wordRaw = wordRaw;
        this.start = start;
        this.direction = direction;
    }

    public static Move pass() {
        return new Move(true, null, null, null);
    }

    public static Move play(String wordRaw, Square start, Direction direction) {
        return new Move(false, wordRaw, start, direction);
    }

    public boolean isPass() { return pass; }
    public String wordRaw() { return wordRaw; }
    public Square start() { return start; }
    public Direction direction() { return direction; }
}
