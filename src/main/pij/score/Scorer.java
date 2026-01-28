package pij.score;

import pij.board.Board;
import pij.board.Cell;
import pij.board.CellType;
import pij.game.Placement;
import pij.game.ValidatedMove;
import pij.tiles.Tile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Scorer {

    /**
     * Computes the score for a validated move, according to the coursework rules.
     * Premiums apply only to newly placed tiles (placements).
     */
    public ScoreBreakdown scoreMove(Board board, ValidatedMove vm) {
        List<Placement> placements = vm.placements();
        if (placements.isEmpty()) {
            return new ScoreBreakdown(0, 1, 0, 0); // pass yields 0
        }

        // Map placements by square for quick lookup
        Map<String, Tile> placed = new HashMap<>();
        for (Placement p : placements) {
            placed.put(key(p.square().row(), p.square().col()), p.tile());
        }

        // Compute base sum for all letters in the main word AFTER the move.
        // We reconstruct by scanning along the word direction using the board state AFTER apply,
        // OR (simpler for now) require scoring is called BEFORE apply and use "afterMove" logic.
        //
        // To keep it simple and robust, call scorer AFTER apply and scan the board for mainWord letters.
        // We will locate the word by searching for the sequence near the placements.
        //
        // Practical approach (minimal): compute base using:
        // - For each placement: tile value * letterMultiplier(if any)
        // - Plus for existing tiles in the main word: tile face value (no multipliers)
        //
        // That requires knowing which squares form the main word path. We'll compute that by locating
        // a contiguous line through all placements in the word direction.
        //
        // Assumption: vm.mainWord is exactly the contiguous letters along that line after apply.
        // We'll find the word line using the min row/col among placements.

        WordLine line = WordLine.from(board, placements);

        int wordMultiplier = 1;
        int baseSum = 0;

        // Walk the word line and add values
        int r = line.startRow;
        int c = line.startCol;
        for (int i = 0; i < vm.mainWord().length(); i++) {
            Cell cell = board.cellAt(r, c);
            Tile tile = cell.tile(); // must exist after apply
            int letterValue = tile.value();

            // letter premium only if THIS square was newly placed this move
            Tile newlyPlaced = placed.get(key(r, c));
            if (newlyPlaced != null) {
                if (cell.type() == CellType.LETTER_PREMIUM) {
                    letterValue *= cell.factor();
                }
                if (cell.type() == CellType.WORD_PREMIUM) {
                    wordMultiplier *= cell.factor();
                }
            }

            baseSum += letterValue;

            r += line.dr;
            c += line.dc;
        }

        int total = baseSum * wordMultiplier;

        int bingoBonus = (placements.size() == 7) ? 60 : 0;
        total += bingoBonus;

        return new ScoreBreakdown(baseSum, wordMultiplier, bingoBonus, total);
    }

    private static String key(int r, int c) { return r + "," + c; }

    /**
     * Represents the main word line direction and starting square.
     * We infer direction from placements: if all rows equal => RIGHT else DOWN.
     * Then choose the smallest row/col placement as anchor and walk backwards to word start.
     */
    private static final class WordLine {
        final int startRow, startCol;
        final int dr, dc;

        private WordLine(int startRow, int startCol, int dr, int dc) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.dr = dr;
            this.dc = dc;
        }

        static WordLine from(Board board, List<Placement> placements) {
            if (placements.isEmpty()) throw new IllegalArgumentException("no placements");

            boolean sameRow = placements.stream().allMatch(p -> p.square().row() == placements.get(0).square().row());
            int dr = sameRow ? 0 : 1;
            int dc = sameRow ? 1 : 0;

            // pick a starting point among placements: minimal (row,col) along direction
            int r = placements.get(0).square().row();
            int c = placements.get(0).square().col();
            for (Placement p : placements) {
                int pr = p.square().row();
                int pc = p.square().col();
                if (sameRow) {
                    if (pr == r && pc < c) { c = pc; }
                } else {
                    if (pc == c && pr < r) { r = pr; }
                }
            }

            // walk backwards until out of bounds or empty
            int br = r;
            int bc = c;
            while (board.inBounds(br - dr, bc - dc) && !board.isEmptyAt(br - dr, bc - dc)) {
                br -= dr;
                bc -= dc;
            }

            return new WordLine(br, bc, dr, dc);
        }
    }
}
