package com.feefo.jobnormaliser.similarity;

@FunctionalInterface
public interface TitleSimilarity {

    /**
     * Calculates a similarity score q between the input and a canonical title.
     * @return double q where 0.0 <= q <= 1.0
     */
    double score(String inputTitle, String candidateTitle);
}
