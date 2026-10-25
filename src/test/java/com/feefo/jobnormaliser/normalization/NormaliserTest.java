package com.feefo.jobnormaliser.normalization;

import com.feefo.jobnormaliser.similarity.JaccardTokenSimilarity;
import com.feefo.jobnormaliser.similarity.LevenshteinSimilarity;
import com.feefo.jobnormaliser.similarity.TitleSimilarity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NormaliserTest {

    private static final List<String> EXAMPLE_TITLES = List.of(
            "Architect",
            "Software engineer",
            "Quantity surveyor",
            "Accountant"
    );

    private final Normaliser normaliser = new Normaliser();

    @ParameterizedTest
    @CsvSource({
            "Java engineer, Software engineer",
            "C# engineer, Software engineer",
            "Chief Accountant, Accountant",
            "Architect, Architect",
            "Quantity surveyor, Quantity surveyor"
    })
    void normalisesRequiredExamples(String input, String expected) {
        assertEquals(Optional.of(expected), normaliser.normalise(input));
    }

    @ParameterizedTest
    @CsvSource({
            "'  SOFTWARE   ENGINEER  ', Software engineer",
            "Chief Accountant!, Accountant"
    })
    void jaccardSimilarityHandlesFormattingDifferences(String input, String expected) {
        assertEquals(Optional.of(expected), normaliser.normalise(input));
    }

    @ParameterizedTest
    @CsvSource({
            "Java engineer, Software engineer",
            "C# engineer, Software engineer",
            "Chief Accountant, Accountant",
            "Accountnt, Accountant",
            "Quantity surveyer, Quantity surveyor"
    })
    void levenshteinSimilarityHandlesRequiredExamplesAndSpellingDifferences(String input, String expected) {
        Normaliser fuzzyNormaliser = new Normaliser(new LevenshteinSimilarity(), 0.50);

        assertEquals(Optional.of(expected), fuzzyNormaliser.normalise(input));
    }

    @Test
    void returnsEmptyWhenEveryScoreIsZero() {
        assertEquals(Optional.empty(), normaliser.normalise("Developer"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.25, 0.50})
    void acceptsCandidateAtConfiguredMinimumScore(double minimumScore) {
        TitleSimilarity similarity = (input, candidate) -> candidate.equals("Architect") ? minimumScore : 0.0;
        Normaliser customNormaliser = new Normaliser(
                List.of("Architect", "Accountant"), similarity, minimumScore
        );

        assertEquals(Optional.of("Architect"), customNormaliser.normalise("Designer"));
    }

    @ParameterizedTest
    @CsvSource({"0.25, 0.249", "0.50, 0.499"})
    void returnsEmptyWhenBestScoreIsBelowConfiguredMinimum(double minimumScore, double score) {
        Normaliser customNormaliser = new Normaliser(
                List.of("Architect"),
                (input, candidate) -> score,
                minimumScore
        );

        assertEquals(Optional.empty(), customNormaliser.normalise("Designer"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Java developer", "Help", "Chef"})
    void returnsEmptyForLowScoringLevenshteinExamples(String input) {
        Normaliser fuzzyNormaliser = new Normaliser(new LevenshteinSimilarity(), 0.50);

        assertEquals(Optional.empty(), fuzzyNormaliser.normalise(input));
    }

    @Test
    void appliesConfiguredMinimumScoresIndependently() {
        TitleSimilarity similarity = (input, candidate) -> 0.4;
        Normaliser lowerCutoffNormaliser = new Normaliser(similarity, 0.25);
        Normaliser higherCutoffNormaliser = new Normaliser(similarity, 0.50);

        assertEquals(Optional.of("Architect"), lowerCutoffNormaliser.normalise("Designer"));
        assertEquals(Optional.empty(), higherCutoffNormaliser.normalise("Designer"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInvalidMinimumScoresAtConstruction(double minimumScore) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Normaliser(new LevenshteinSimilarity(), minimumScore)
        );
    }

    @Test
    void acceptsZeroScoreWhenMinimumIsZero() {
        Normaliser customNormaliser = new Normaliser(
                List.of("Architect"), (input, candidate) -> 0.0, 0.0
        );

        assertEquals(Optional.of("Architect"), customNormaliser.normalise("Designer"));
    }

    @Test
    void acceptsPerfectScoreWhenMinimumIsOne() {
        Normaliser exactNormaliser = new Normaliser(new LevenshteinSimilarity(), 1.0);

        assertEquals(Optional.of("Architect"), exactNormaliser.normalise("Architect"));
    }

    @Test
    void rejectsImperfectScoreWhenMinimumIsOne() {
        Normaliser exactNormaliser = new Normaliser(new LevenshteinSimilarity(), 1.0);

        assertEquals(Optional.empty(), exactNormaliser.normalise("Architec"));
    }

    @Test
    void rejectsNullInput() {
        assertThrows(NullPointerException.class, () -> normaliser.normalise(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "!!!"})
    void rejectsInputWithoutLettersOrNumbers(String input) {
        assertThrows(IllegalArgumentException.class, () -> normaliser.normalise(input));
    }

    @Test
    void resolvesEqualScoresUsingConfiguredTitleOrder() {
        assertEquals(Optional.of("Architect"), normaliser.normalise("architect accountant"));
    }

    @Test
    void supportsCustomTitlesAndSimilarityStrategies() {
        TitleSimilarity fixedSimilarity = (input, candidate) -> candidate.equals("Developer") ? 0.8 : 0.4;
        Normaliser customNormaliser = new Normaliser(List.of("Tester", "Developer"), fixedSimilarity, 0.25);

        assertEquals(Optional.of("Developer"), customNormaliser.normalise("anything"));
    }

    @Test
    void passesOriginalInputToSimilarityStrategy() {
        TitleSimilarity similarity = (input, candidate) -> {
            assertEquals(" JAVA-Engineer! ", input);
            return 1.0;
        };
        Normaliser customNormaliser = new Normaliser(List.of("Software engineer"), similarity, 0.25);

        assertEquals(Optional.of("Software engineer"), customNormaliser.normalise(" JAVA-Engineer! "));
    }

    @Test
    void copiesCustomTitlesAtConstruction() {
        List<String> titles = new ArrayList<>(List.of("Architect"));
        Normaliser customNormaliser = new Normaliser(titles, new JaccardTokenSimilarity(), 0.25);
        titles.set(0, "Accountant");

        assertEquals(Optional.of("Architect"), customNormaliser.normalise("Architect"));
    }

    @Test
    void rejectsInvalidTitleConfigurationAtConstruction() {
        assertThrows(
                NullPointerException.class,
                () -> new Normaliser(null, new JaccardTokenSimilarity(), 0.25)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Normaliser(List.of(), new JaccardTokenSimilarity(), 0.25)
        );
        assertThrows(
                NullPointerException.class,
                () -> new Normaliser(Arrays.asList("Architect", null), new JaccardTokenSimilarity(), 0.25)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Normaliser(List.of("Architect", "  "), new JaccardTokenSimilarity(), 0.25)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new Normaliser(List.of("Architect", "!!!"), new JaccardTokenSimilarity(), 0.25)
        );
    }

    @Test
    void acceptsEquivalentTitlesAndUsesTheFirstOnATie() {
        Normaliser customNormaliser = new Normaliser(
                List.of("Software-Engineer", "software engineer"), new JaccardTokenSimilarity(), 0.25
        );

        assertEquals(Optional.of("Software-Engineer"), customNormaliser.normalise("Software engineer"));
    }

    @Test
    void rejectsNullSimilarity() {
        assertThrows(NullPointerException.class, () -> new Normaliser(null, 0.25));
        assertThrows(NullPointerException.class, () -> new Normaliser(EXAMPLE_TITLES, null, 0.25));
    }

    @ParameterizedTest
    @CsvSource({"-0.1", "1.1", "NaN", "Infinity"})
    void rejectsScoresOutsideTheSimilarityContract(String scoreText) {
        double invalidScore = switch (scoreText) {
            case "NaN" -> Double.NaN;
            case "Infinity" -> Double.POSITIVE_INFINITY;
            default -> Double.parseDouble(scoreText);
        };
        Normaliser invalidNormaliser = new Normaliser(EXAMPLE_TITLES, (input, candidate) -> invalidScore, 0.25);

        assertThrows(IllegalStateException.class, () -> invalidNormaliser.normalise("Designer"));
    }
}
