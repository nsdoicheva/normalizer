package com.feefo.jobnormaliser.similarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevenshteinSimilarityTest {

    private final LevenshteinSimilarity similarity = new LevenshteinSimilarity();

    private static final double DELTA = 1e-9;

    @Test
    void shouldReturnZeroWhenEitherArgumentIsNull() {
        assertEquals(0.0, similarity.score(null, "Accountant"));
        assertEquals(0.0, similarity.score("Accountant", null));
        assertEquals(0.0, similarity.score(null, null));
    }

    @Test
    void shouldReturnOneWhenBothTitlesAreEmptyAfterNormalization() {
        assertEquals(1.0, similarity.score("", ""));
        assertEquals(1.0, similarity.score("   ", ""));
        assertEquals(1.0, similarity.score("", "   "));
    }

    @Test
    void shouldReturnZeroWhenOnlyOneTitleIsEmpty() {
        assertEquals(0.0, similarity.score("", "Accountant"));
        assertEquals(0.0, similarity.score("Accountant", ""));
        assertEquals(0.0, similarity.score("   ", "Accountant"));
        assertEquals(0.0, similarity.score("Accountant", "   "));
    }

    @Test
    void shouldReturnOneWhenTitlesAreIdentical() {
        assertEquals(1.0, similarity.score("Accountant", "Accountant")
        );
    }

    @Test
    void shouldReturnOneWhenTitlesAreEquivalentAfterNormalization() {
        assertEquals(1.0,
                similarity.score(" Software-Engineer! ","software engineer")
        );
    }

    @Test
    void shouldReturnExpectedScoreWhenOneCharacterIsInserted() {
        assertEquals(
                8.0 / 9.0,
                similarity.score("engineer", "engineers"),
                DELTA
        );
    }

    @Test
    void shouldReturnExpectedScoreWhenOneCharacterIsDeleted() {
        assertEquals(
                0.9,
                similarity.score("Accountant", "Accountnt"),
                DELTA
        );
    }

    @Test
    void shouldReturnExpectedScoreWhenOneCharacterIsSubstituted() {
        assertEquals(
                16.0 / 17.0,
                similarity.score("Quantity surveyer", "Quantity surveyor"),
                DELTA
        );
    }

    @Test
    void shouldReturnZeroWhenEveryCharacterMustBeSubstituted() {
        assertEquals(0.0, similarity.score("abc", "xyz"));
    }

    @Test
    void shouldReturnSameScoreWhenArgumentsAreReversed() {
        assertEquals(
                similarity.score("Java engineer", "Software engineer"),
                similarity.score("Software engineer", "Java engineer"),
                DELTA
        );
    }

    @Test
    void shouldReturnScoreWithinRequiredRangeWhenTitlesDiffer() {
        double score = similarity.score("Java engineer", "Software engineer");
        assertTrue(score >= 0.0 && score <= 1.0);
    }

}
