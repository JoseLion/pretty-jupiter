package io.github.joselion.prettyjupiter.lib;

import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.internal.tasks.testing.DecoratingTestDescriptor;
import org.gradle.api.internal.tasks.testing.DefaultTestDescriptor;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.api.tasks.testing.TestResult.ResultType;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import io.github.joselion.prettyjupiter.lib.helpers.Color;
import io.github.joselion.prettyjupiter.lib.helpers.Icon;
import io.github.joselion.prettyjupiter.lib.helpers.Text;
import testing.Helpers;
import testing.MockDescriptor;
import testing.annotations.UnitTest;

@UnitTest class PrettyLoggerTest {

  @Nested class logDescriptors {
    @Nested class when_the_test_is_not_parameterized {
      @Nested class and_the_descriptor_is_Gradles_initial {
        @Test void does_not_log_Gradle_Test_title() {
          final var logger = mock(Logger.class);
          final var prettyLogger = prettyLoggerOf(logger);
          final var descriptor = MockDescriptor.empty().withDisplayName("Gradle Test");

          prettyLogger.logDescriptors(descriptor);

          verify(logger, never()).lifecycle(contains("Gradle Test"));
          verify(logger, never()).lifecycle("\u001B[97mGradle Test\u001B[0m");
        }
      }

      @Nested class and_the_descriptor_is_of_a_test_suite {
        @Nested class and_the_descriptor_className_is_not_present {
          @Test void does_not_log_the_colored_display_name() {
            final var logger = mock(Logger.class);
            final var prettyLogger = prettyLoggerOf(logger);
            final var descriptor = MockDescriptor.empty().withDisplayName("This is a test description!");

            prettyLogger.logDescriptors(descriptor);

            verify(logger, never()).lifecycle(contains("Gradle Test"));
            verify(logger, never()).lifecycle("\u001B[97mGradle Test\u001B[0m");
          }
        }

        @Nested class and_the_descriptor_className_is_present {
          @Test void logs_the_colored_display_name() {
            final var logger = mock(Logger.class);
            final var prettyLogger = prettyLoggerOf(logger);
            final var descriptor = MockDescriptor.empty()
              .withDisplayName("This is a test description!")
              .withClassName("SomeClassTest");

            prettyLogger.logDescriptors(descriptor);

            verify(logger, only()).lifecycle("\u001B[97mThis is a test description!\u001B[0m");
          }
        }
      }
    }

    @Nested class when_the_test_is_parameterized {
      @Nested class and_the_descriptor_is_Gradles_initial {
        @Test void does_not_log_Gradle_Test_title() {
          final var logger = mock(Logger.class);
          final var prettyLogger = prettyLoggerOf(logger);
          final var descriptor = mock(DecoratingTestDescriptor.class);
          when(descriptor.getDisplayName()).thenReturn("Gradle Test");

          prettyLogger.logDescriptors(descriptor);

          verify(logger, never()).lifecycle(contains("Gradle Test"));
          verify(logger, never()).lifecycle("\u001B[97mGradle Test\u001B[0m");
        }
      }

      @Nested class and_the_descriptor_is_of_a_test_suite {
        @Test void logs_the_colored_display_name() {
          final var logger = mock(Logger.class);
          final var prettyLogger = prettyLoggerOf(logger);
          final var descriptor = mock(DecoratingTestDescriptor.class);
          when(descriptor.getDisplayName()).thenReturn("This is a parameterized test description!");
          when(descriptor.getParent()).thenReturn(new DefaultTestDescriptor("id", null, "Gradle Test"));

          prettyLogger.logDescriptors(descriptor);

          verify(logger, only()).lifecycle("\u001B[97mThis is a parameterized test description!\u001B[0m");
        }
      }
    }
  }

  @Nested class logResults {
    @Nested class when_the_result_has_different_types {
      @TestFactory Stream<DynamicTest> logs_the_result_with_the_icon_and_colors_of_the_type() {
        return Map.of(
          ResultType.SUCCESS, Icon.SUCCESS + " \u001B[90mThis is a test result!\u001B[0m (\u001B[97m10ms\u001B[0m)",
          ResultType.FAILURE, Icon.FAILURE + " \u001B[31mThis is a test result!\u001B[0m (\u001B[97m10ms\u001B[0m)",
          ResultType.SKIPPED, Icon.SKIPPED + " \u001B[33mThis is a test result!\u001B[0m (\u001B[97m10ms\u001B[0m)"
        )
        .entrySet()
        .stream()
        .map(entry ->
          dynamicTest("[status: %s]".formatted(entry.getKey()), () -> {
            final var logger = mock(Logger.class);
            final var prettyLogger = prettyLoggerOf(logger);
            final var descriptor = MockDescriptor.empty().withDisplayName("This is a test result!");
            final var result = mock(TestResult.class);
            when(result.getResultType()).thenReturn(entry.getKey());
            when(result.getStartTime()).thenReturn(10000L);
            when(result.getEndTime()).thenReturn(10010L);
            when(result.getException()).thenReturn(
              entry.getKey().equals(ResultType.FAILURE)
                ? new AssertionError("Expected something to match some other")
                : null
            );

            prettyLogger.logResults(descriptor, result);

            verify(logger, only()).lifecycle(entry.getValue());
          })
        );
      }
    }

    @Nested class when_the_result_has_different_durations {
      @TestFactory Stream<DynamicTest> logs_the_result_with_the_matching_duration_color() {
        return Stream.of(
          entry(Color.RED, 100L),
          entry(Color.RED, 75L),
          entry(Color.YELLOW, 50L),
          entry(Color.YELLOW, 38L),
          entry(Color.WHITE, 25L),
          entry(Color.WHITE, 0L)
        )
        .map(entry ->
          dynamicTest("[duration: %d]".formatted(entry.getValue()), () -> {
            final var logger = mock(Logger.class);
            final var prettyLogger = prettyLoggerOf(logger);
            final var descriptor = MockDescriptor.empty().withDisplayName("This is a test result!");
            final var result = mock(TestResult.class);
            when(result.getResultType()).thenReturn(ResultType.SUCCESS);
            when(result.getStartTime()).thenReturn(0L);
            when(result.getEndTime()).thenReturn(entry.getValue());

            prettyLogger.logResults(descriptor, result);

            final var color = entry.getKey().getCode();
            final var expected = "%s \u001B[90mThis is a test result!\u001B[0m (\u001B[%sm%sms\u001B[0m)".formatted(
              Icon.SUCCESS,
              color,
              entry.getValue()
            );

            verify(logger, only()).lifecycle(expected);
          })
        );
      }
    }

    @Nested class when_the_duration_is_disabled {
      @Test void logs_the_result_without_the_duration() {
        final var logger = mock(Logger.class);
        final var prettyLogger = prettyLoggerOf(logger, ext -> ext.getDuration().getEnabled().set(false));
        final var descriptor = MockDescriptor.empty().withDisplayName("Some tests without duration");
        final var result = mock(TestResult.class);
        when(result.getResultType()).thenReturn(ResultType.SUCCESS);

        prettyLogger.logResults(descriptor, result);

        verify(logger, only()).lifecycle(Icon.SUCCESS + " \u001B[90mSome tests without duration\u001B[0m");
      }
    }
  }

  @Nested class logSummary {
    @Nested class when_the_descriptor_is_not_the_last {
      @Test void deos_not_log_the_summary() {
        final var logger = mock(Logger.class);
        final var prettyLogger = prettyLoggerOf(logger);
        final var descriptor = Helpers.descriptorOf(2);
        final var result = mock(TestResult.class);

        prettyLogger.logSummary(descriptor, result);

        verify(logger, never()).lifecycle(anyString());
      }
    }

    @Nested class when_the_descriptor_is_the_last {
      @TestFactory Stream<DynamicTest> logs_the_summary_based_on_the_result() { // NOSONAR
        return Map.of(
          ResultType.SUCCESS, Icon.SUCCESS,
          ResultType.FAILURE, Icon.FAILURE,
          ResultType.SKIPPED, Icon.SKIPPED
        )
        .entrySet()
        .stream()
        .map(entry ->
          dynamicTest("[result: %s]".formatted(entry.getKey()), () -> {
            final var logger = mock(Logger.class);
            final var prettyLogger = prettyLoggerOf(logger);
            final var descriptor = Helpers.descriptorOf(0);
            final var causeD = new Exception("Cause of error C");
            final var causeC = new Exception("Cause of error B", causeD);
            final var causeB = new Exception("Cause of error A", causeC);
            final var causeA = new Exception("Cause of top error", causeB);
            final var exception = new Exception("\nMulti\nline\nexception!", causeA);
            final var testRes1 = mock(TestResult.class);
            when(testRes1.getResultType()).thenReturn(ResultType.FAILURE);
            when(testRes1.getException()).thenReturn(exception);
            final var testRes2 = mock(TestResult.class);
            when(testRes2.getResultType()).thenReturn(ResultType.FAILURE);
            when(testRes2.getException()).thenReturn(causeD);
            final var results = mock(TestResult.class);
            when(results.getResultType()).thenReturn(entry.getKey());
            when(results.getStartTime()).thenReturn(1583909261673L);
            when(results.getEndTime()).thenReturn(1583909325290L);
            when(results.getTestCount()).thenReturn(136L);
            when(results.getSuccessfulTestCount()).thenReturn(120L);
            when(results.getFailedTestCount()).thenReturn(10L);
            when(results.getSkippedTestCount()).thenReturn(6L);

            prettyLogger.logResults(Helpers.descriptorOf(2), testRes1);
            prettyLogger.logResults(Helpers.descriptorOf(2), testRes2);
            prettyLogger.logSummary(descriptor, results);

            final var plainSummary = """
              %s 136 tests completed, \u001B[32m120 succeed\u001B[0m, \u001B[31m10 failed\u001B[0m, \
              \u001B[33m6 skipped\u001B[0m (1 minutes, 3.617 seconds)\
              """
              .formatted(entry.getValue());
            final var visibleText = Text.uncolored(plainSummary);
            final var plainReport = "Report: path/to/report/file.html";
            final var reportTrailSpace = " ".repeat(visibleText.length() - plainReport.length());
            final var hidden1 = exception.getStackTrace().length - 10;
            final var hidden2 = causeD.getStackTrace().length - 10;
            final var failedTest = Icon.FAILURE + " \u001B[31mTest description 1\u001B[0m (\u001B[97m0ms\u001B[0m)";
            final var ordered = inOrder(logger);

            ordered.verify(logger, times(2)).lifecycle(failedTest);
            ordered.verify(logger).lifecycle("\n");
            ordered.verify(logger).lifecycle("\u001B[91m(1)\u001B[0m  Test description 1:");
            ordered.verify(logger).lifecycle("       \u001B[91mjava.lang.Exception: ");
            ordered.verify(logger).lifecycle("       Multi");
            ordered.verify(logger).lifecycle("       line");
            ordered.verify(logger).lifecycle("       exception!\u001B[0m");
            ordered.verify(logger).lifecycle("");
            ordered.verify(logger).lifecycle("     Caused by:");
            ordered.verify(logger).lifecycle("       \u001B[33m+ java.lang.Exception: Cause of top error");
            ordered.verify(logger).lifecycle("       └─┬─ java.lang.Exception: Cause of error A");
            ordered.verify(logger).lifecycle("         └─┬─ java.lang.Exception: Cause of error B");
            ordered.verify(logger).lifecycle("           └─── java.lang.Exception: Cause of error C\u001B[0m");
            ordered.verify(logger).lifecycle("");
            ordered.verify(logger).lifecycle("     Stack trace:");
            ordered.verify(logger).lifecycle("       \u001B[90mjava.lang.Exception:  Multi line exception!");
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[0]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[1]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[2]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[3]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[4]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[5]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[6]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[7]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[8]);
            ordered.verify(logger).lifecycle("         at " + exception.getStackTrace()[9]);
            ordered.verify(logger).lifecycle("         --- and " + hidden1 + " more ---\u001B[0m");
            ordered.verify(logger).lifecycle("\n");
            ordered.verify(logger).lifecycle("\u001B[91m(2)\u001B[0m  Test description 1:");
            ordered.verify(logger).lifecycle("       \u001B[91mjava.lang.Exception: Cause of error C\u001B[0m");
            ordered.verify(logger).lifecycle("");
            ordered.verify(logger).lifecycle("     Stack trace:");
            ordered.verify(logger).lifecycle("       \u001B[90mjava.lang.Exception: Cause of error C");
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[0]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[1]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[2]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[3]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[4]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[5]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[6]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[7]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[8]);
            ordered.verify(logger).lifecycle("         at " + causeD.getStackTrace()[9]);
            ordered.verify(logger).lifecycle("         --- and " + hidden2 + " more ---\u001B[0m");
            ordered.verify(logger).lifecycle("\n");
            ordered.verify(logger).lifecycle("╔═" + "═".repeat(visibleText.length()) + "══╗");
            ordered.verify(logger).lifecycle("║ " + plainSummary + "  ║");
            ordered.verify(logger).lifecycle("║ " + " ".repeat(visibleText.length()) + "  ║");
            ordered.verify(logger).lifecycle("║ " + plainReport + reportTrailSpace + "  ║");
            ordered.verify(logger).lifecycle("╚═" + "═".repeat(visibleText.length()) + "══╝");
            ordered.verifyNoMoreInteractions();
          })
        );
      }
    }
  }

  private PrettyLogger prettyLoggerOf(final Logger loggerSpy, final Consumer<PrettyJupiterExtension> updater) {
    final var project = mock(Project.class);
    final var testTask = mock(
      org.gradle.api.tasks.testing.Test.class,
      withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
    );
    final var extension = ProjectBuilder
      .builder()
      .build()
      .getObjects()
      .newInstance(PrettyJupiterExtension.class);

    updater.accept(extension);
    when(project.getLogger()).thenReturn(loggerSpy);
    when(testTask.getReports().getHtml().getEntryPoint().toString()).thenReturn("path/to/report/file.html");

    return PrettyLogger.of(project, testTask, extension);
  }

  private PrettyLogger prettyLoggerOf(final Logger loggerSpy) {
    return this.prettyLoggerOf(loggerSpy, x -> { });
  }
}
