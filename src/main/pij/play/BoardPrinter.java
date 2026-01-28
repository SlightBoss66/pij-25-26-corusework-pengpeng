package pij.play;

import pij.board.Board;
import pij.board.Cell;
import pij.tiles.Tile;

public final class BoardPrinter {

    public void print(Board b) {
        int rows = b.rows();
        int cols = b.cols();

        System.out.print("   ");
        for (int c = 0; c < cols; c++) {
            System.out.print((char)('a' + c) + " ");
        }
        System.out.println();

        for (int r = 0; r < rows; r++) {
            String rowLabel = String.format("%2d ", r + 1);
            System.out.print(rowLabel);

            for (int c = 0; c < cols; c++) {
                Cell cell = b.cellAt(r, c);
                if (!cell.isEmpty()) {
                    Tile t = cell.tile();
                    char ch = t.isWildcard() ? t.displayChar() : t.letter();
                    System.out.print(Character.toUpperCase(ch) + " ");
                } else {
                    String s = switch (cell.type()) {
                        case NORMAL -> ".";
                        case LETTER_PREMIUM -> "L";
                        case WORD_PREMIUM -> "W";
                    };
                    System.out.print(s + " ");
                }
            }
            System.out.println();
        }
    }
}
