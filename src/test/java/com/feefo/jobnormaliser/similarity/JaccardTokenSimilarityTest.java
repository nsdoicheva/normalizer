package com.feefo.jobnormaliser.similarity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JaccardTokenSimilarityTest {

    private final JaccardTokenSimilarity similarity = new JaccardTokenSimilarity();

    @Test
    void shouldReturnZeroWhenEitherArgumentIsNull() {
        assertEquals(0.0, similarity.score(null, "Accountant"));
        assertEquals(0.0, similarity.score("Accountant", null));
        assertEquals(0.0, similarity.score(null, null));
    }

    @Test
    void shouldReturnOneWhenBothTitlesAreEmpty() {
        assertEquals(1.0, similarity.score("", ""));
        assertEquals(1.0, similarity.score("   ", ""));
    }

    @Test
    void shouldReturnZeroWhenOnlyOneTitleIsEmpty() {
        assertEquals(0.0, similarity.score("   ", "Accountant"));
        assertEquals(0.0, similarity.score("Accountant", "   "));
    }

    @Test
    void shouldReturnOneWhenTitlesDifferOnlyInCase() {
        assertEquals(1.0,
                similarity.score("Software engineer", "software engineer")
        );
    }

    @Test
    void shouldReturnSharedTokenCountDividedByUnionSizeWhenTokensOverlap() {
        assertEquals(
                1.0 / 3.0,
                similarity.score("Java engineer", "Software engineer"),
                1e-9
        );
    }

    @Test
    void shouldIgnorePunctuationWhenComparingTokens() {
        assertEquals(0.5,
                similarity.score("Chief Accountant!", "Accountant")
        );
    }

    @Test
    void shouldReturnZeroWhenNoTokensAreShared() {
        assertEquals(0.0, similarity.score("Developer", "Accountant"));
    }

    @Test
    void shouldReturnZeroWhenTokensDifferByASpellingError() {
        assertEquals(0.0, similarity.score("Accountnt", "Accountant"));
    }

    @Test
    void shouldCountEachTokenOnceWhenTokensAreRepeated() {
        assertEquals(0.5, similarity.score("Chief Chief Accountant", "Accountant")
        );
    }
}

