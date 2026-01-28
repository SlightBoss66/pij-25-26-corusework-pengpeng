package pij.tiles;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TileRackBagTest {

    @Test
    void wildcardHasFixedValueEight() {
        Tile w = Tile.wildcard();
        assertTrue(w.isWildcard());
        assertEquals(8, w.value());
        assertEquals('_', w.letter());
    }

    @Test
    void wildcardCanBeChosenOnce() {
        Tile w = Tile.wildcard();
        w.chooseLetter('e');
        assertEquals('e', w.displayChar());
        // choosing same letter again is ok
        w.chooseLetter('E');
        assertEquals('e', w.displayChar());
    }

    @Test
    void wildcardRejectsChangingChosenLetter() {
        Tile w = Tile.wildcard();
        w.chooseLetter('e');
        assertThrows(IllegalStateException.class, () -> w.chooseLetter('a'));
    }

    @Test
    void rackTakeLetterRemovesTile() {
        Rack r = new Rack();
        r.add(Tile.normal('A', 1));
        r.add(Tile.normal('B', 3));
        assertTrue(r.takeLetter('B').isPresent());
        assertEquals(1, r.size());
        assertFalse(r.takeLetter('B').isPresent());
    }

    @Test
    void rackTakeWildcardRemovesWildcard() {
        Rack r = new Rack();
        r.add(Tile.normal('A', 1));
        r.add(Tile.wildcard());
        assertTrue(r.takeWildcard().isPresent());
        assertEquals(1, r.size());
        assertFalse(r.takeWildcard().isPresent());
    }

    @Test
    void rackTotalValueSumsFaceValues() {
        Rack r = new Rack();
        r.add(Tile.normal('A', 1));
        r.add(Tile.normal('B', 3));
        r.add(Tile.wildcard()); // 8
        assertEquals(12, r.totalValue());
    }

    @Test
    void bagDrawReducesSize() {
        TileBag bag = new TileBag(List.of(Tile.normal('A', 1), Tile.normal('B', 3)), new Random(0));
        assertEquals(2, bag.size());
        assertEquals(1, bag.draw(1).size());
        assertEquals(1, bag.size());
    }

    @Test
    void bagRefillRackUpToTargetOrUntilEmpty() {
        TileBag bag = new TileBag(List.of(
                Tile.normal('A', 1),
                Tile.normal('B', 3),
                Tile.normal('C', 3)
        ), new Random(0));

        Rack r = new Rack();
        bag.refillRack(r, 7);
        assertEquals(3, r.size());
        assertTrue(bag.isEmpty());
    }
}
