# Job title normaliser

A Java 17 Maven project that maps an input job title to the best match in a list of canonical titles. The default titles are **Architect**, **Software engineer**, **Quantity surveyor**, and **Accountant**.

## Requirements

- JDK 17 or newer
- Maven 3.x, or an IDE with Maven support

## Import, run, and test

Open the directory containing `pom.xml` as a Maven project in your IDE and select JDK 17 or newer. Run `com.feefo.jobnormaliser.Main` to see the required examples.

From the project root, you can run the same checks in a terminal:

```sh
mvn test
java -cp target/classes com.feefo.jobnormaliser.Main
```

`mvn test` compiles the project and runs the JUnit tests before the `java` command runs the example program. This project does not include a Maven wrapper, so use an installed Maven or the Maven bundled with your IDE.

## Usage

```java
import com.feefo.jobnormaliser.normalization.Normaliser;
import com.feefo.jobnormaliser.similarity.JaccardTokenSimilarity;
import com.feefo.jobnormaliser.similarity.LevenshteinSimilarity;

import java.util.List;
import java.util.Optional;

Normaliser normaliser = new Normaliser(); // Jaccard with a 0.25 cutoff
Optional<String> title = normaliser.normalise("Java engineer"); // Optional[Software engineer]
String displayedTitle = title.orElse("No matching job title found");

Normaliser fuzzyNormaliser = new Normaliser(new LevenshteinSimilarity(), 0.50);
Optional<String> corrected = fuzzyNormaliser.normalise("Accountnt"); // Optional[Accountant]
Optional<String> unmatched = fuzzyNormaliser.normalise("Help"); // Optional.empty()

Normaliser customNormaliser = new Normaliser(List.of("Developer", "Tester"),
        new JaccardTokenSimilarity(), 0.25);
Optional<String> customResult = customNormaliser.normalise("Java developer"); // Optional[Developer]
```

The default strategy is `JaccardSimilarity` over distinct normalized words, with a **0.25** cutoff.
`LevenshteinSimilarity` compares normalized whole titles by character edits; the example configures it with a **0.50** cutoff. Both return a quality score between 0.0 and 1.0 internally. `Normaliser` returns an `Optional<String>` containing the highest-scoring candidate only when its score reaches that instance's configured minimum; otherwise, it returns `Optional.empty()`. A score equal to the minimum qualifies. 