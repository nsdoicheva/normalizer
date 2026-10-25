package com.feefo.jobnormaliser.similarity;

import com.feefo.jobnormaliser.normalization.JobTitleTokenizer;

import java.util.HashSet;
import java.util.Set;

/**
 * Calculates Jaccard similarity for the distinct normalized tokens in two job titles.
 * The score is {@code |A ∩ B| / |A ∪ B|}, where {@code A} and {@code B} are the
 * titles' token sets. Two empty sets score 1.0; a null title scores 0.0.
 *
 * <p>Tokens match exactly: {@code engineer} and {@code enginer} are distinct tokens
 * and contribute no intersection.
 */
public class JaccardTokenSimilarity implements TitleSimilarity {

    @Override
    public double score(String inputTitle, String candidateTitle) {
        if (inputTitle == null || candidateTitle == null) {
            return 0.0;
        }

        Set<String> inputTokens = new HashSet<>(JobTitleTokenizer.tokenize(inputTitle));
        Set<String> candidateTokens = new HashSet<>(JobTitleTokenizer.tokenize(candidateTitle));

        if (inputTokens.isEmpty() && candidateTokens.isEmpty()) {
            return 1.0;
        }
        if (inputTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(inputTokens);
        intersection.retainAll(candidateTokens);

        Set<String> union = new HashSet<>(inputTokens);
        union.addAll(candidateTokens);

        return (double) intersection.size() / union.size();
    }
}
