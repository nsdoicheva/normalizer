package com.feefo.jobnormaliser;

import com.feefo.jobnormaliser.normalization.Normaliser;
import com.feefo.jobnormaliser.similarity.LevenshteinSimilarity;

public class Main {

    public static void main(String[] args) {
        Normaliser jaccard = new Normaliser();
        Normaliser levenshtein = new Normaliser(new LevenshteinSimilarity(), 0.50);

        System.out.println("--- Jaccard Normaliser (cutoff 0.25) ---");
        run(jaccard, "Java engineer");
        run(jaccard, "C# engineer");
        run(jaccard, "Chief Accountant");

        System.out.println("\n--- Levenshtein Normaliser (cutoff 0.50) ---");
        run(levenshtein, "Accountnt");
        run(levenshtein, "Quantity surveyer");
        run(levenshtein, "Java developer");
        run(levenshtein, "Chef");
    }

    private static void run(Normaliser normaliser, String input) {
        String result = normaliser.normalise(input)
                .orElse("No matching job title found");
        System.out.printf("Input: %-22s -> Result: %s%n", "\"" + input + "\"", result);
    }
}
