package pij.ai;

import pij.board.Board;
import pij.game.IllegalMoveException;
import pij.game.ValidatedMove;
import pij.move.Move;
import pij.tiles.Rack;

public interface PlayerController {

    /**
     * Offer a move for this player.
     * May return a pass move.
     */
    Move chooseMove(Board board, Rack rack, boolean firstMove);
}
