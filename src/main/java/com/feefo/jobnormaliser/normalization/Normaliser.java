package com.feefo.jobnormaliser.normalization;

import com.feefo.jobnormaliser.similarity.JaccardTokenSimilarity;
import com.feefo.jobnormaliser.similarity.TitleSimilarity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Selects the best matching title from a fixed list of canonical job titles.
 * The first configured title wins when scores are equal.
 */
public class Normaliser {
    private static final double DEFAULT_MINIMUM_SCORE = 0.25;

    private static final List<String> DEFAULT_TITLES = List.of(
            "Architect",
            "Software engineer",
            "Quantity surveyor",
            "Accountant"
    );

    private final List<String> candidates;
    private final TitleSimilarity similarity;
    private final double minimumScore;

    /** Creates a normaliser with the default titles, Jaccard scoring, and a 0.25 cutoff. */
    public Normaliser() {
        this(DEFAULT_TITLES, new JaccardTokenSimilarity(), DEFAULT_MINIMUM_SCORE);
    }

    /**
     * Creates a normaliser with the default titles and an explicitly configured scoring policy.
     *
     * @param similarity strategy that scores an input against each title from 0.0 to 1.0
     * @param minimumScore finite minimum accepted score from 0.0 to 1.0, inclusive
     * @throws NullPointerException if the strategy is null
     * @throws IllegalArgumentException if the minimum score is not finite or is outside 0.0 to 1.0
     */
    public Normaliser(TitleSimilarity similarity, double minimumScore) {
        this(DEFAULT_TITLES, similarity, minimumScore);
    }

    /**
     * Creates a normaliser with a snapshot of the supplied titles and a similarity strategy.
     *
     * @param normalisedTitles non-empty list of canonical job titles
     * @param similarity strategy that scores an input against each title from 0.0 to 1.0
     * @param minimumScore finite minimum accepted score from 0.0 to 1.0, inclusive
     * @throws NullPointerException if the title list or strategy is null, or a title is null
     * @throws IllegalArgumentException if the list is empty, a title has no letters or numbers,
     *         or the minimum score is not finite or is outside 0.0 to 1.0
     */
    public Normaliser(List<String> normalisedTitles, TitleSimilarity similarity, double minimumScore) {
        this.similarity = Objects.requireNonNull(similarity, "Title similarity cannot be null");
        this.candidates = validateAndCopyTitles(normalisedTitles);
        if (!Double.isFinite(minimumScore) || minimumScore < 0.0 || minimumScore > 1.0) {
            throw new IllegalArgumentException("Minimum score must be finite and between 0.0 and 1.0");
        }
        this.minimumScore = minimumScore;
    }

    /**
     * Returns the configured title with the highest similarity score for the input.
     * The score must reach the configured minimum; if scores tie, the title listed first wins.
     *
     * @param jobTitle non-null job title to normalise
     * @return an optional containing the best matching canonical title,
     *         or an empty optional if no score reaches the configured minimum
     * @throws NullPointerException if {@code jobTitle} is null
     * @throws IllegalArgumentException if {@code jobTitle} contains no letters or numbers
     * @throws IllegalStateException if the strategy returns a score outside 0.0 to 1.0
     */
    public Optional<String> normalise(String jobTitle) {
        Objects.requireNonNull(jobTitle, "Job title cannot be null");
        if (JobTitleTokenizer.normalise(jobTitle).isEmpty()) {
            throw new IllegalArgumentException("Job title must contain letters or numbers");
        }
        return findBestMatch(jobTitle);
    }

    /**
     * Scores the configured candidates and selects the highest-scoring title.
     * The first configured title wins a tie. A result is returned only when
     * its score reaches the configured minimum.
     *
     * @param jobTitle validated input title passed unchanged to the strategy
     * @return an optional containing the best matching canonical title,
     *         or an empty optional if its score is below the configured minimum
     * @throws IllegalStateException if the strategy returns a non-finite score
     *         or a score outside {@code [0.0, 1.0]}
     */
    private Optional<String> findBestMatch(String jobTitle) {
        String bestMatch = candidates.get(0);
        double maxScore = -1.0;

        for (String candidate : candidates) {
            double score = similarity.score(jobTitle, candidate);
            validateScore(score, candidate);
            if (score > maxScore) {
                maxScore = score;
                bestMatch = candidate;
            }
        }

        if (maxScore < minimumScore) {
            return Optional.empty();
        }
        return Optional.of(bestMatch);
    }

    private static List<String> validateAndCopyTitles(List<String> normalisedTitles) {
        List<String> titles = List.copyOf(normalisedTitles);
        if (titles.isEmpty()) {
            throw new IllegalArgumentException("Normalised titles cannot be empty");
        }

        for (String title : titles) {
            if (JobTitleTokenizer.normalise(title).isEmpty()) {
                throw new IllegalArgumentException("Normalised titles must contain letters or numbers");
            }
        }

        return titles;
    }

    private static void validateScore(double score, String candidate) {
        if (!Double.isFinite(score) || score < 0.0 || score > 1.0) {
            throw new IllegalStateException(
                    "Title similarity returned an invalid score for '" + candidate + "': " + score
            );
        }
    }
}
