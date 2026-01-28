package pij.tiles;

import java.util.Objects;
import java.util.Optional;

public final class Tile {
    private final char letter;          // For wildcard, store '_' (underscore)
    private final int value;            // Wildcard value fixed to 8
    private final boolean wildcard;
    private Character chosenLetter;     // for wildcard only; null until chosen

    private Tile(char letter, int value, boolean wildcard) {
        this.letter = letter;
        this.value = value;
        this.wildcard = wildcard;
    }

    public static Tile normal(char letter, int value) {
        if (!Character.isLetter(letter)) throw new IllegalArgumentException("letter must be A-Z");
        return new Tile(Character.toUpperCase(letter), value, false);
    }

    public static Tile wildcard() {
        return new Tile('_', 8, true);
    }

    public boolean isWildcard() { return wildcard; }

    /** For normal tiles, this is the uppercase letter. For wildcard, this is '_' */
    public char letter() { return letter; }

    /** Value of the tile. Wildcard is always 8. */
    public int value() { return value; }

    /** For wildcard, returns chosen letter if already bound; otherwise empty. */
    public Optional<Character> chosenLetter() {
        return Optional.ofNullable(chosenLetter);
    }

    /**
     * Bind this wildcard tile to a chosen letter (lowercase in display).
     * Can only be called once; subsequent calls with a different letter are illegal.
     */
    public void chooseLetter(char c) {
        if (!wildcard) throw new IllegalStateException("Not a wildcard tile");
        if (!Character.isLetter(c)) throw new IllegalArgumentException("Chosen letter must be a-z");
        char lower = Character.toLowerCase(c);
        if (chosenLetter == null) {
            chosenLetter = lower;
        } else if (!Objects.equals(chosenLetter, lower)) {
            throw new IllegalStateException("Wildcard already chosen: " + chosenLetter);
        }
    }

    /**
     * For UI/debug: normal tiles show as 'A'; wildcard shows as chosen lowercase if it is set,
     * otherwise '_' (you can later print as [_8] at rack level).
     */
    public char displayChar() {
        if (!wildcard) return letter;
        return chosenLetter != null ? chosenLetter : '_';
    }
}
