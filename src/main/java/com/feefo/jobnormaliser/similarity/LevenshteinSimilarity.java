package com.feefo.jobnormaliser.similarity;

import com.feefo.jobnormaliser.normalization.JobTitleTokenizer;

/**
 * Compares whole job titles using Levenshtein edit distance after normalisation.
 * The score is {@code 1.0 - (double) distance / longestTitleLength}, where
 * {@code longestTitleLength} is the length of the longer normalized title.
 * Identical normalized titles score 1.0; small spelling differences can receive
 * a partial score, and word order affects the result.
 *
 * <p>A null title scores 0.0. Two titles that normalize to empty strings score
 * 1.0, while exactly one empty title scores 0.0.
 */
public class LevenshteinSimilarity implements TitleSimilarity {

    @Override
    public double score(String inputTitle, String candidateTitle) {
        if (inputTitle == null || candidateTitle == null) {
            return 0.0;
        }

        String normalisedInputTitle = JobTitleTokenizer.normalise(inputTitle);
        String normalisedCandidateTitle = JobTitleTokenizer.normalise(candidateTitle);
        if (normalisedInputTitle.equals(normalisedCandidateTitle)) {
            return 1.0;
        }
        if (normalisedInputTitle.isEmpty() || normalisedCandidateTitle.isEmpty()) {
            return 0.0;
        }

        int distance = calculateDistance(normalisedInputTitle, normalisedCandidateTitle);
        int longestTitleLength = Math.max(normalisedInputTitle.length(), normalisedCandidateTitle.length());
        double normalisedDistance = (double) distance / longestTitleLength;
        return 1.0 - normalisedDistance;
    }

    /**
     * Computes the edit distance between two text-normalized titles. Inserting,
     * deleting, or substituting one UTF-16 code unit each costs one edit.
     *
     * @param inputTitle the normalized input title
     * @param candidateTitle the normalized candidate title
     * @return the minimum number of edits needed to turn the input into the candidate
     */
    private int calculateDistance(String inputTitle, String candidateTitle) {
        int[] previousRow = new int[candidateTitle.length() + 1];
        int[] currentRow = new int[candidateTitle.length() + 1];
        for (int column = 0; column <= candidateTitle.length(); column++) {
            previousRow[column] = column;
        }

        for (int row = 1; row <= inputTitle.length(); row++) {
            currentRow[0] = row;
            for (int column = 1; column <= candidateTitle.length(); column++) {
                int substitutionCost = inputTitle.charAt(row - 1) == candidateTitle.charAt(column - 1) ? 0 : 1;
                currentRow[column] = Math.min(
                        Math.min(currentRow[column - 1] + 1, previousRow[column] + 1),
                        previousRow[column - 1] + substitutionCost
                );
            }
            int[] completedRow = previousRow;
            previousRow = currentRow;
            currentRow = completedRow;
        }

        return previousRow[candidateTitle.length()];
    }
}
