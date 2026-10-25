package com.feefo.jobnormaliser.normalization;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Prepares job-title text for similarity comparisons. */
public final class JobTitleTokenizer {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");

    private JobTitleTokenizer() {
    }

    /**
     * Applies Unicode NFKC normalization and locale-independent lowercase, replaces runs
     * of non-letter and non-number characters with spaces, and trims the result.
     *
     * @param text non-null job-title text
     * @return the normalized text, or an empty string if no letters or numbers remain
     * @throws NullPointerException if {@code text} is null
     */
    public static String normalise(String text) {
        Objects.requireNonNull(text, "text must not be null");

        String normalizedText = Normalizer
                .normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);

        return NON_ALPHANUMERIC.matcher(normalizedText)
                .replaceAll(" ")
                .trim();
    }

    /**
     * Normalizes a job title and returns its tokens in input order, including repeated words.
     *
     * @param text non-null job-title text
     * @return an immutable list of tokens, or an empty list if no letters or numbers remain
     * @throws NullPointerException if {@code text} is null
     */
    public static List<String> tokenize(String text) {
        String normalizedText = normalise(text);

        if (normalizedText.isEmpty()) {
            return List.of();
        }

        return List.of(normalizedText.split("\\s+"));
    }
}
