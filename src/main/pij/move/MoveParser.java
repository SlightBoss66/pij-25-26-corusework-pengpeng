package pij.move;

import pij.board.Direction;
import pij.board.Square;

public final class MoveParser {

    public Move parse(String line, int boardColsM, int boardRowsN) throws MoveFormatException {
        if (line == null) throw new MoveFormatException("Illegal move format");

        String t = line.trim();

        // pass: exactly ","
        if (t.equals(",")) {
            return Move.pass();
        }

        int comma = t.indexOf(',');
        if (comma < 0 || comma != t.lastIndexOf(',')) {
            throw new MoveFormatException("Illegal move format");
        }

        String word = t.substring(0, comma).trim();
        String squareToken = t.substring(comma + 1).trim();

        if (word.isEmpty() || squareToken.isEmpty()) {
            throw new MoveFormatException("Illegal move format");
        }

        if (!word.matches("^[A-Za-z]+$")) {
            throw new MoveFormatException("Illegal move format");
        }

        ParsedSquare ps = parseSquareToken(squareToken, boardColsM, boardRowsN);
        return Move.play(word, ps.square, ps.dir);
    }

    private static final class ParsedSquare {
        final Square square;
        final Direction dir;
        ParsedSquare(Square square, Direction dir) {
            this.square = square;
            this.dir = dir;
        }
    }

    // "f8" => DOWN (col letter + row number)
    // "10b" => RIGHT (row number + col letter)
    private ParsedSquare parseSquareToken(String s, int mCols, int nRows) throws MoveFormatException {
        String t = s.trim().toLowerCase();

        // case 1: letter + digits
        if (t.length() >= 2 && Character.isLetter(t.charAt(0)) && Character.isDigit(t.charAt(1))) {
            Square sq;
            try {
                sq = Square.parseColumnRow(t, mCols, nRows);
            } catch (IllegalArgumentException e) {
                throw new MoveFormatException("Illegal move format");
            }
            return new ParsedSquare(sq, Direction.DOWN);
        }

        // case 2: digits + letter (row first)
        if (t.length() >= 2 && Character.isDigit(t.charAt(0)) && Character.isLetter(t.charAt(t.length()-1))) {
            String rowPart = t.substring(0, t.length()-1);
            char colChar = t.charAt(t.length()-1);

            int row1Based;
            try {
                row1Based = Integer.parseInt(rowPart);
            } catch (NumberFormatException e) {
                throw new MoveFormatException("Illegal move format");
            }

            int col = colChar - 'a';
            int row0 = row1Based - 1;
            if (col < 0 || col >= mCols || row0 < 0 || row0 >= nRows) {
                throw new MoveFormatException("Illegal move format");
            }
            return new ParsedSquare(new Square(row0, col), Direction.RIGHT);
        }

        throw new MoveFormatException("Illegal move format");
    }
}
