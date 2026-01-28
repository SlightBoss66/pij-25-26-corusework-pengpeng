package pij.ai;

import pij.board.Board;
import pij.board.Direction;
import pij.board.Square;
import pij.dict.WordList;
import pij.game.IllegalMoveException;
import pij.game.MoveValidator;
import pij.game.ValidatedMove;
import pij.move.Move;
import pij.tiles.Rack;
import pij.tiles.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class SimpleComputer implements PlayerController {

    private final MoveValidator validator;
    private final WordList dict;
    private final Random rng;

    public SimpleComputer(MoveValidator validator, WordList dict) {
        this(validator, dict, new Random());
    }

    public SimpleComputer(MoveValidator validator, WordList dict, Random rng) {
        this.validator = validator;
        this.dict = dict;
        this.rng = rng;
    }

    @Override
    public Move chooseMove(Board board, Rack rack, boolean firstMove) {
        // 1) Collect candidate dictionary words of length 2..7 that are feasible with current rack
        List<String> candidates = new ArrayList<>();
        for (String w : dict.allWords()) {
            int len = w.length();
            if (len < 2 || len > 7) continue;

            // Check if we can build this word using rack letters + wildcards,
            // and also construct the "wordRaw" using lowercase for wildcard-used letters.
            String wordRaw = buildWordRawIfPossible(w, rack);
            if (wordRaw != null) candidates.add(wordRaw);
        }

        // Randomize a bit so AI doesn't always play same pattern
        Collections.shuffle(candidates, rng);

        // 2) Try each candidate, scan board for a legal placement
        for (String wordRaw : candidates) {
            // Optional: try random directions order
            Direction[] dirs = Direction.values();
            if (rng.nextBoolean()) {
                dirs = new Direction[]{Direction.RIGHT, Direction.DOWN};
            } else {
                dirs = new Direction[]{Direction.DOWN, Direction.RIGHT};
            }

            for (Direction d : dirs) {
                for (int r = 0; r < board.rows(); r++) {
                    for (int c = 0; c < board.cols(); c++) {
                        Move m = Move.play(wordRaw, new Square(r, c), d);
                        try {
                            ValidatedMove vm = validator.validate(board, rack, m, firstMove, dict);
                            // Found a legal move
                            return m;
                        } catch (IllegalMoveException ignored) {
                            // try next
                        }
                    }
                }
            }
        }

        // 3) Nothing legal found
        return Move.pass();
    }

    /**
     * Given a dictionary word (uppercase), try to build a Move.wordRaw string using:
     * - uppercase letters if we have the real tile in rack
     * - lowercase letters if we need to consume a wildcard tile
     *
     * Returns null if the word cannot be formed from the rack.
     */
    private String buildWordRawIfPossible(String dictWordUpper, Rack rack) {
        int[] available = new int[26];
        int wildcards = 0;

        for (Tile t : rack.tilesView()) {
            if (t.isWildcard()) {
                wildcards++;
            } else {
                char ch = t.letter(); // uppercase
                if (ch >= 'A' && ch <= 'Z') available[ch - 'A']++;
            }
        }

        StringBuilder sb = new StringBuilder(dictWordUpper.length());

        for (int i = 0; i < dictWordUpper.length(); i++) {
            char ch = dictWordUpper.charAt(i);
            if (ch < 'A' || ch > 'Z') return null; // defensive

            int idx = ch - 'A';
            if (available[idx] > 0) {
                available[idx]--;
                sb.append(ch); // real tile -> uppercase
            } else if (wildcards > 0) {
                wildcards--;
                sb.append(Character.toLowerCase(ch)); // wildcard -> lowercase
            } else {
                return null; // cannot form
            }
        }

        return sb.toString();
    }
}