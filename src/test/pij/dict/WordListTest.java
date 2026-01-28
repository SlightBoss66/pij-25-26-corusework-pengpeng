package pij.dict;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WordListTest {

    @Test
    void containsIsCaseInsensitive() {
        WordList wl = new WordList(Set.of("HELLO", "WORLD"));
        assertTrue(wl.contains("hello"));
        assertTrue(wl.contains("HeLLo"));
        assertFalse(wl.contains("nope"));
    }

    @Test
    void resourceLoadsIfPresent() throws Exception {
        // This only checks that the resource exists and can be read,
        // without asserting specific dictionary contents.
        WordList wl = WordList.fromResource("/wordlist.txt");
        assertTrue(wl.size() > 0);
    }
}