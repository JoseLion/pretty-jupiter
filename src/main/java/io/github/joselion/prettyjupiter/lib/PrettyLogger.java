package io.github.joselion.prettyjupiter.lib;

import static java.util.stream.Collectors.joining;
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.internal.tasks.testing.DecoratingTestDescriptor;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.api.tasks.testing.TestResult.ResultType;

import io.github.joselion.prettyjupiter.lib.helpers.Color;
import io.github.joselion.prettyjupiter.lib.helpers.Common;
import io.github.joselion.prettyjupiter.lib.helpers.Icon;
import io.github.joselion.prettyjupiter.lib.helpers.Text;

public record PrettyLogger(
  Project project,
  Test testTask,
  PrettyJupiterExtension extension,
  List<Failure> failures
) {

  public static PrettyLogger of(final Project project, final Test testTask, final PrettyJupiterExtension extension) {
    return new PrettyLogger(
      project,
      testTask,
      extension,
      new ArrayList<>()
    );
  }

  public void logDescriptors(final TestDescriptor descriptor) {
    final var displayName = descriptor.getDisplayName();
    final var isDecorationDescriptor = descriptor instanceof DecoratingTestDescriptor;
    final var isParameterizedTest = isDecorationDescriptor && !displayName.contains("Gradle Test");

    if (descriptor.getClassName() != null || isParameterizedTest) {
      final var tabs = Common.tabsFor(descriptor);
      final var desc = Text.colored(Color.WHITE, displayName);

      this.project.getLogger().lifecycle(tabs.concat(desc));
    }
  }

  public void logResults(final TestDescriptor descriptor, final TestResult result) {
    final var status = metaOf(result.getResultType());
    final var tabs = Common.tabsFor(descriptor);
    final var desc = Text.colored(status.color(), descriptor.getDisplayName());
    final var duration = getDuration(result);

    if (result.getResultType().equals(FAILURE)) {
      this.failures.add(Failure.of(result.getException(), descriptor, extension));
    }

    project.getLogger().lifecycle(tabs.concat(status.icon().toString()).concat(" ").concat(desc).concat(duration));
  }

  public void logSummary(final TestDescriptor descriptor, final TestResult result) {
    if (descriptor.getParent() == null) {
      IntStream
        .range(0, this.failures.size())
        .forEach(i -> {
          final var failure = this.failures.get(i);
          final var n = Text.colored(Color.BRIGHT_RED, "(".concat(Integer.toString(i + 1)).concat(")"));
          final var ns = " ".repeat(Integer.toString(i).length() + 2);

          project.getLogger().lifecycle("\n");

          project.getLogger().lifecycle(n.concat("  ").concat(failure.location()).concat(":"));
          failure.message().lines().forEach(line ->
            project.getLogger().lifecycle(ns.concat("    ").concat(line))
          );

          if (failure.cause() != null) {
            project.getLogger().lifecycle("");
            project.getLogger().lifecycle(ns.concat("  Caused by:"));
            failure.cause().lines().forEach(line ->
              project.getLogger().lifecycle(ns.concat("    ").concat(line))
            );
          }

          project.getLogger().lifecycle("");
          project.getLogger().lifecycle(ns.concat("  Stack trace:"));
          failure.trace().lines().forEach(line ->
            project.getLogger().lifecycle(ns.concat("    ").concat(line))
          );
        });

      final var status = metaOf(result.getResultType());
      final var succeed = Text.colored(Color.GREEN, Long.toString(result.getSuccessfulTestCount()).concat(" succeed"));
      final var failed = Text.colored(Color.RED, Long.toString(result.getFailedTestCount()).concat(" failed"));
      final var skipped = Text.colored(Color.YELLOW, Long.toString(result.getSkippedTestCount()).concat(" skipped"));
      final var time = Duration.ofMillis(result.getEndTime()).minusMillis(result.getStartTime());
      final var stats = "%s %d tests completed, %s, %s, %s (%.3f seconds)".formatted(
        status.icon(),
        result.getTestCount(),
        succeed,
        failed,
        skipped,
        time.toMillis() / 1000.0
      );
      final var report = "Report: ".concat(this.testTask.getReports().getHtml().getEntryPoint().toString());
      final var summary = Stream.of(stats, "", report).collect(joining("\n"));
      final var max = summary.lines()
        .map(Text::uncolored)
        .map(String::length)
        .max(Integer::compare)
        .map(x -> x + 1)
        .orElse(1);

      project.getLogger().lifecycle("\n");
      project.getLogger().lifecycle("╔═".concat("═".repeat(max)).concat("═╗"));
      summary.lines().forEach(line -> {
        final var ws = " ".repeat(max - Text.uncolored(line).length());
        project.getLogger().lifecycle("║ ".concat(line).concat(ws).concat(" ║"));
      });
      project.getLogger().lifecycle("╚═".concat("═".repeat(max)).concat("═╝"));
    }
  }

  private String getDuration(final TestResult result) {
    final var duration = this.extension.getDuration();

    if (duration.getEnabled().get().booleanValue()) {
      final var timeDiff = result.getEndTime() - result.getStartTime();
      final var threshold = duration.getThreshold(testTask);
      final var halfColor = timeDiff >= threshold / 2 ? Color.YELLOW : Color.WHITE;
      final var color = timeDiff >= threshold ? Color.RED : halfColor;
      final var millis = Text.colored(color, Long.toString(timeDiff).concat("ms"));

      return " (".concat(millis).concat(")");
    }

    return "";
  }

  private StatusMeta metaOf(final ResultType resultType) {
    return switch (resultType) {
      case SUCCESS -> new StatusMeta(Icon.SUCCESS, Color.GRAY);
      case FAILURE -> new StatusMeta(Icon.FAILURE, Color.RED);
      case SKIPPED -> new StatusMeta(Icon.SKIPPED, Color.YELLOW);
    };
  }

  record StatusMeta(Icon icon, Color color) { }
}
