package pij.board;

/**
 * Represents a single board cell.
 * Premium factors apply only when a tile is placed on this cell.
 */
public final class Cell {
    private final CellType type;
    private final int factor; // for premium cells; for normal we keep 1

    public Cell(CellType type, int factor) {
        this.type = type;
        this.factor = factor;
    }

    public CellType type() { return type; }
    public int factor() { return factor; }

    public static Cell normal() { return new Cell(CellType.NORMAL, 1); }
    public static Cell letter(int factor) { return new Cell(CellType.LETTER_PREMIUM, factor); }
    public static Cell word(int factor) { return new Cell(CellType.WORD_PREMIUM, factor); }
}