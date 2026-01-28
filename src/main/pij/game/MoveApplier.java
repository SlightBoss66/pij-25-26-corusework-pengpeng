package pij.game;

import pij.board.Board;
import pij.tiles.Rack;

public final class MoveApplier {

    /**
     * Applies a validated move to the board and removes the used tiles from the rack.
     * Assumes the move has already been validated.
     */

    public void apply(Board board, Rack rack, ValidatedMove vm) {
        for (Placement p : vm.placements()) {
            board.placeTile(p.square().row(), p.square().col(), p.tile());
        }
        // NOTE: rack removal already happened during validation planning if you choose destructive planning.
        // In our design, validation is non-destructive, so we must remove from the real rack here.
        // We'll provide a helper in MoveValidator to compute placements using a rackCopy,
        // but also return the exact tiles to remove. Easiest: remove by matching properties.
        removeTilesFromRack(rack, vm);
    }

    private void removeTilesFromRack(Rack rack, ValidatedMove vm) {
        // Remove each placement tile from the rack.
        // For normal tiles, remove by letter. For wildcard, remove a wildcard.
        for (Placement p : vm.placements()) {
            if (p.tile().isWildcard()) {
                rack.takeWildcard(); // should exist
            } else {
                rack.takeLetter(p.tile().letter()); // should exist
            }
        }
    }
}
