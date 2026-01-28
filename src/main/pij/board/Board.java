package pij.board;

/**
 * Immutable representation of the game board.
 * Stores board dimensions, start square, and cell layout.
 */

public final class Board {
    private final int mCols;
    private final int nRows;
    private final Square startSquare;
    private final Cell[][] cells; //  [row][col]

    public Board(int mCols, int nRows, Square startSquare, Cell[][] cells) {
        this.mCols = mCols;
        this.nRows = nRows;
        this.startSquare = startSquare;
        this.cells = cells;
    }

    public int cols() { return mCols; }
    public int rows() { return nRows; }
    public Square startSquare() { return startSquare; }

    public Cell cellAt(int row, int col) {
        return cells[row][col];
    }
}
