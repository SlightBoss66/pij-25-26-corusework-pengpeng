package pij.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Loads and queries the dictionary from resources/wordlist.txt.
 * Words are stored in uppercase for case-insensitive lookup.
 */
public final class WordList {
    private final Set<String> words;

    public WordList(Set<String> words) {
        this.words = new HashSet<>(words);
    }

    /** Loads the dictionary from a classpath resource (e.g., "/wordlist.txt"). */
    public static WordList fromResource(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath is blank");
        }

        InputStream in = WordList.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        Set<String> set = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String w = line.trim();
                if (w.isEmpty()) continue;
                // tolerate lower/upper in file; normalize
                set.add(w.toUpperCase(Locale.ROOT));
            }
        }
        return new WordList(set);
    }

    /** Case-insensitive lookup. Returns true iff the word exists in the dictionary. */
    public boolean contains(String word) {
        if (word == null) return false;
        String w = word.trim();
        if (w.isEmpty()) return false;
        return words.contains(w.toUpperCase(Locale.ROOT));
    }

    public int size() {
        return words.size();
    }
}
