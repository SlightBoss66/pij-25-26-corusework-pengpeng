package pij.board;

import pij.tiles.Tile;

/**
 * Represents a single board cell.
 * Premium factors apply only when a tile is placed on this cell.
 */
public final class Cell {
    private final CellType type;
    private final int factor;

    private Tile tile; // null means empty

    public Cell(CellType type, int factor) {
        this.type = type;
        this.factor = factor;
    }

    public CellType type() { return type; }
    public int factor() { return factor; }

    // ===== 新增方法 =====

    public boolean isEmpty() {
        return tile == null;
    }

    public Tile tile() {
        return tile;
    }

    public void place(Tile tile) {
        if (tile == null) throw new IllegalArgumentException("tile is null");
        if (this.tile != null) throw new IllegalStateException("cell already occupied");
        this.tile = tile;
    }

    // 工厂方法保持不变
    public static Cell normal() { return new Cell(CellType.NORMAL, 1); }
    public static Cell letter(int factor) { return new Cell(CellType.LETTER_PREMIUM, factor); }
    public static Cell word(int factor) { return new Cell(CellType.WORD_PREMIUM, factor); }
}