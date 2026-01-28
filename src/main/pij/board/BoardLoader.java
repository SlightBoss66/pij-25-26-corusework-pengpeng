package pij.board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads a game board from a text file as specified in the coursework.
 * Validates board dimensions, start square, and premium cell tokens.
 * Throws InvalidBoardFileException if the file violates any constraints.
 */

public final class BoardLoader {

    private static final Pattern LETTER = Pattern.compile("^\\[(-?\\d{1,2})\\]$");
    private static final Pattern WORD   = Pattern.compile("^<(-?\\d{1,2})>$");

    public Board load(Path file) throws IOException, InvalidBoardFileException {
        try (BufferedReader br = Files.newBufferedReader(file)) {
            return load(br);
        }
    }

    public Board load(Reader reader) throws IOException, InvalidBoardFileException {
        BufferedReader br = (reader instanceof BufferedReader)
                ? (BufferedReader) reader
                : new BufferedReader(reader);

        int m = parseIntLine(br, "M");
        int n = parseIntLine(br, "N");

        // constraints
        if (m < 7 || m > 26) throw new InvalidBoardFileException("M out of range: " + m);
        if (n < 10 || n > 99) throw new InvalidBoardFileException("N out of range: " + n);
        if ((long)m * (long)n < 192) throw new InvalidBoardFileException("M*N must be >= 192");

        String startLine = br.readLine();
        if (startLine == null) throw new InvalidBoardFileException("Missing start square");
        Square start;
        try {
            start = Square.parseColumnRow(startLine, m, n);
        } catch (IllegalArgumentException e) {
            throw new InvalidBoardFileException("Invalid start square: " + startLine, e);
        }

        Cell[][] cells = new Cell[n][m];

        for (int row = 0; row < n; row++) {
            String line = br.readLine();
            if (line == null) throw new InvalidBoardFileException("Missing board row: " + (row + 1));

            String[] tokens = line.trim().isEmpty() ? new String[0] : line.trim().split("\\s+");
            if (tokens.length != m) {
                throw new InvalidBoardFileException("Row " + (row + 1) + " has " + tokens.length + " tokens; expected " + m);
            }

            for (int col = 0; col < m; col++) {
                cells[row][col] = parseCell(tokens[col]);
            }
        }

        return new Board(m, n, start, cells);
    }

    private static int parseIntLine(BufferedReader br, String name) throws IOException, InvalidBoardFileException {
        String s = br.readLine();
        if (s == null) throw new InvalidBoardFileException("Missing " + name);
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            throw new InvalidBoardFileException("Invalid " + name + ": " + s, e);
        }
    }

    private static Cell parseCell(String token) throws InvalidBoardFileException {
        if (token.equals(".")) return Cell.normal();

        Matcher ml = LETTER.matcher(token);
        if (ml.matches()) {
            int factor = Integer.parseInt(ml.group(1));
            checkFactor(factor);
            return Cell.letter(factor);
        }

        Matcher mw = WORD.matcher(token);
        if (mw.matches()) {
            int factor = Integer.parseInt(mw.group(1));
            checkFactor(factor);
            return Cell.word(factor);
        }

        throw new InvalidBoardFileException("Invalid token: " + token);
    }

    private static void checkFactor(int factor) throws InvalidBoardFileException {
        if (factor < -9 || factor > 99) throw new InvalidBoardFileException("Factor out of range: " + factor);
    }
}