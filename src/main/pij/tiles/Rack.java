package pij.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public final class Rack {
    private final List<Tile> tiles = new ArrayList<>();

    public List<Tile> tilesView() {
        return Collections.unmodifiableList(tiles);
    }

    public int size() { return tiles.size(); }

    public void add(Tile t) {
        if (t == null) throw new IllegalArgumentException("tile null");
        tiles.add(t);
    }

    public void addAll(List<Tile> ts) {
        for (Tile t : ts) add(t);
    }

    public boolean isEmpty() { return tiles.isEmpty(); }

    /**
     * Removes one normal tile matching the given letter (case-insensitive).
     * Returns removed tile if found.
     */
    public Optional<Tile> takeLetter(char letter) {
        char up = Character.toUpperCase(letter);
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (!t.isWildcard() && t.letter() == up) {
                tiles.remove(i);
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Removes one wildcard tile if available.
     */
    public Optional<Tile> takeWildcard() {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t.isWildcard()) {
                tiles.remove(i);
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Convenience: total face value of remaining tiles (used for end-game penalty).
     */
    public int totalValue() {
        int sum = 0;
        for (Tile t : tiles) sum += t.value();
        return sum;
    }
}
