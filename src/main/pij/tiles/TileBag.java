package pij.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**

 */
public final class TileBag {
    private final List<Tile> bag;
    private final Random rng;

    public TileBag(List<Tile> initialTiles) {
        this(initialTiles, new Random());
    }

    public TileBag(List<Tile> initialTiles, Random rng) {
        if (initialTiles == null) throw new IllegalArgumentException("tiles null");
        this.bag = new ArrayList<>(initialTiles);
        this.rng = rng == null ? new Random() : rng;
        Collections.shuffle(this.bag, this.rng);
    }

    public int size() { return bag.size(); }
    public boolean isEmpty() { return bag.isEmpty(); }

    /** Draw up to k tiles (less if bag has fewer). */
    public List<Tile> draw(int k) {
        if (k < 0) throw new IllegalArgumentException("k < 0");
        int n = Math.min(k, bag.size());
        List<Tile> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(bag.remove(bag.size() - 1)); // pop from end
        }
        return out;
    }

    /** Fill the rack up to targetSize (typically 7) or until bag empty. */
    public void refillRack(Rack rack, int targetSize) {
        if (rack == null) throw new IllegalArgumentException("rack null");
        if (targetSize < 0) throw new IllegalArgumentException("targetSize < 0");
        int need = Math.max(0, targetSize - rack.size());
        rack.addAll(draw(need));
    }

    /**
     * Default distribution (placeholder).
     * If README/resources specify an exact distribution, we will replace this with the required one.
     */
    public static List<Tile> defaultTiles() {
        List<Tile> tiles = new ArrayList<>();
        // A small Scrabble-like sample; adjust later if spec provides exact list.
        addMany(tiles, 'E', 1, 12);
        addMany(tiles, 'A', 1, 9);
        addMany(tiles, 'I', 1, 9);
        addMany(tiles, 'O', 1, 8);
        addMany(tiles, 'N', 1, 6);
        addMany(tiles, 'R', 1, 6);
        addMany(tiles, 'T', 1, 6);
        addMany(tiles, 'L', 1, 4);
        addMany(tiles, 'S', 1, 4);
        addMany(tiles, 'U', 1, 4);

        addMany(tiles, 'D', 2, 4);
        addMany(tiles, 'G', 2, 3);

        addMany(tiles, 'B', 3, 2);
        addMany(tiles, 'C', 3, 2);
        addMany(tiles, 'M', 3, 2);
        addMany(tiles, 'P', 3, 2);

        addMany(tiles, 'F', 4, 2);
        addMany(tiles, 'H', 4, 2);
        addMany(tiles, 'V', 4, 2);
        addMany(tiles, 'W', 4, 2);
        addMany(tiles, 'Y', 4, 2);

        addMany(tiles, 'K', 5, 1);

        addMany(tiles, 'J', 8, 1);
        addMany(tiles, 'X', 8, 1);

        addMany(tiles, 'Q', 10, 1);
        addMany(tiles, 'Z', 10, 1);

        // Wildcards (spec: [_8])
        tiles.add(Tile.wildcard());
        tiles.add(Tile.wildcard());

        return tiles;
    }

    private static void addMany(List<Tile> tiles, char letter, int value, int count) {
        for (int i = 0; i < count; i++) tiles.add(Tile.normal(letter, value));
    }
}
