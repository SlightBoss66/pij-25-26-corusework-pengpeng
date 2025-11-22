package pij.board;

import java.util.Objects;

public final class Square {
    private final int row; // 0-based
    private final int col; // 0-based

    public Square(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() { return row; }
    public int col() { return col; }

    public static Square parseColumnRow(String s, int mCols, int nRows) {
        // format like "d7" (col letter, row number)
        if (s == null) throw new IllegalArgumentException("null square");
        String t = s.trim();
        if (t.length() < 2) throw new IllegalArgumentException("bad square: " + s);

        char c = t.charAt(0);
        if (c < 'a' || c > 'z') throw new IllegalArgumentException("bad square: " + s);

        int col = c - 'a';
        int row;
        try {
            row = Integer.parseInt(t.substring(1)) - 1; // rows are 1-based in input
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad square: " + s);
        }

        if (col < 0 || col >= mCols || row < 0 || row >= nRows) {
            throw new IllegalArgumentException("square out of bounds: " + s);
        }
        return new Square(row, col);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Square)) return false;
        Square square = (Square) o;
        return row == square.row && col == square.col;
    }
    @Override public int hashCode() { return Objects.hash(row, col); }

    @Override public String toString() {
        return "" + (char)('a' + col) + (row + 1);
    }
}
