package io.github.joselion.prettyjupiter.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import testing.Helpers;
import testing.annotations.UnitTest;

@UnitTest class FailureTest {

  private static final PrettyJupiterExtension EXT = ProjectBuilder
    .builder()
    .build()
    .getObjects()
    .newInstance(PrettyJupiterExtension.class);

  @Nested class cause {
    @Nested class when_the_exception_has_no_cause {
      @Test void returns_null() {
        final var failure = Failure.of(new Exception(), Helpers.descriptorOf(0), EXT);

        assertThat(failure.cause()).isNull();
      }
    }

    @Nested class when_the_exceptions_has_cause {
      @TestFactory Stream<DynamicTest> returns_the_cause_tree_text() {
        final var causeD = new Exception("Cause of error C");
        final var causeC = new Exception("Cause of error B", causeD);
        final var causeB = new Exception("Cause of error A", causeC);
        final var causeA = new AssertionError("Cause of top error", causeB);
        final var fullCause = new Exception("Top error", causeA);

        return Map.of(
          causeC, "\u001B[33m+ java.lang.Exception: Cause of error C\u001B[0m",
          causeB, """
                  \u001B[33m+ java.lang.Exception: Cause of error B
                  └─── java.lang.Exception: Cause of error C\u001B[0m\
                  """,
          causeA, """
                  \u001B[33m+ java.lang.Exception: Cause of error A
                  └─┬─ java.lang.Exception: Cause of error B
                    └─── java.lang.Exception: Cause of error C\u001B[0m\
                  """,
          fullCause, """
                     \u001B[33m+ java.lang.AssertionError: Cause of top error
                     └─┬─ java.lang.Exception: Cause of error A
                       └─┬─ java.lang.Exception: Cause of error B
                         └─── java.lang.Exception: Cause of error C\u001B[0m\
                     """
        )
        .entrySet()
        .stream()
        .map(entry ->
          dynamicTest("[error: %s]".formatted(entry.getKey().getMessage()), () -> {
            final var failure = Failure.of(entry.getKey(), Helpers.descriptorOf(1), EXT);

            assertThat(failure.cause()).isEqualTo(entry.getValue());
          })
        );
      }
    }

    @Nested class location {
      @TestFactory Stream<DynamicTest> returns_the_location_based_on_the_descriptor_level() {
        final var results = List.of(
          "",
          "",
          "Test description 1",
          """
          Test description 1
            Test description 2\
          """,
          """
          Test description 1
            Test description 2
              Test description 3\
          """,
          """
          Test description 1
            Test description 2
              Test description 3
                Test description 4\
          """
        );

        return IntStream.range(0, results.size())
        .mapToObj(i -> dynamicTest("[level: %d]".formatted(i), () -> {
          final var failure = Failure.of(new Exception(), Helpers.descriptorOf(i), EXT);
          final var result = results.get(i);

          assertThat(failure.location()).isEqualTo(result);
        }));
      }
    }

    @Nested class message {
      @Test void returns_the_error_message() {
        final var error = "This should be an Assertion error!";
        final var failure = Failure.of(new Exception(error), Helpers.descriptorOf(2), EXT);

        assertThat(failure.message()).isEqualTo("\u001B[91mjava.lang.Exception: %s\u001B[0m".formatted(error));
      }
    }

    @Nested class trace {
      @Test void returns_the_error_stack_trace() {
        final var exception = new Exception("Some error message");
        final var failure = Failure.of(exception, Helpers.descriptorOf(2), EXT);
        final var maxTrace = EXT.getFailure().getMaxTraceLines().get();
        final var traceDiff = exception.getStackTrace().length - maxTrace;
        final var lines = failure.trace().lines().toList();

        assertThat(lines)
          .hasSize(maxTrace + 2)
          .filteredOn(line -> lines.indexOf(line) > 0)
          .allSatisfy(line -> assertThat(line).startsWith("  "));

        assertThat(lines) // NOSONAR
          .first()
          .isEqualTo("\u001B[90mjava.lang.Exception: Some error message");
        assertThat(lines)
          .last()
          .isEqualTo("  --- and %s more ---\u001B[0m".formatted(traceDiff));
      }
    }
  }
}
