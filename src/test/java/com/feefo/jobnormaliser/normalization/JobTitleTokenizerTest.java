package com.feefo.jobnormaliser.normalization;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobTitleTokenizerTest {

    @Test
    void shouldThrowNullPointerExceptionWhenInputIsNull() {
        assertThrows(NullPointerException.class, () -> JobTitleTokenizer.normalise(null));
        assertThrows(NullPointerException.class, () -> JobTitleTokenizer.tokenize(null));
    }

    @Test
    void shouldReturnLowercaseTextWhenInputContainsUppercaseLetters() {
        assertEquals("chief accountant", JobTitleTokenizer.normalise("CHIEF Accountant"));
    }

    @Test
    void shouldReturnEmptyTextWhenInputContainsOnlyPunctuation() {
        assertEquals("", JobTitleTokenizer.normalise("!@#---"));
    }

    @Test
    void shouldCollapseSeparatorsWhenInputContainsPunctuationAndWhitespace() {
        assertEquals("developer v2", JobTitleTokenizer.normalise("  Developer / \t V2!!  "));
    }

    @Test
    void shouldReturnEmptyTokensWhenInputContainsNoLettersOrNumbers() {
        assertEquals(List.of(), JobTitleTokenizer.tokenize(" \t! -- "));
    }

    @Test
    void shouldPreserveTokenOrderWhenInputContainsMultipleWords() {
        assertEquals(
                List.of("architect", "java", "engineer"),
                JobTitleTokenizer.tokenize("Architect / Java engineer")
        );
    }

    @Test
    void shouldPreserveRepeatedTokensWhenInputContainsDuplicateWords() {
        assertEquals(
                List.of("engineer", "java", "engineer", "java", "architect"),
                JobTitleTokenizer.tokenize("Engineer, Java / engineer JAVA architect")
        );
    }
}
