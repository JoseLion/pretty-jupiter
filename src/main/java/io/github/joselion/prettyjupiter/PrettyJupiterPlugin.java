package io.github.joselion.prettyjupiter;

import static io.github.joselion.prettyjupiter.lib.helpers.Common.closure;

import java.util.Set;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestLogEvent;

import io.github.joselion.prettyjupiter.lib.PrettyJupiterExtension;
import io.github.joselion.prettyjupiter.lib.PrettyLogger;

/**
 * A Gradle plugin to better log JUnit Jupiter tests when run from the Gradle
 * test task. Nested tests are grouped and the output styles is very similar to
 * {@code mocha.js}
 */
public class PrettyJupiterPlugin implements Plugin<Project> {

  @Override
  public void apply(final Project project) {
    final var extension = project.getExtensions().create("prettyJupiter", PrettyJupiterExtension.class);

    project.getTasks().withType(Test.class).configureEach(testTask -> {
      final var prettyLogger = PrettyLogger.of(project, testTask, extension);
      final var testLogging = testTask.getTestLogging();
      final var reports = testTask.getReports();

      testLogging.setEvents(Set.of(TestLogEvent.STANDARD_ERROR));
      testLogging.setShowExceptions(false);
      testLogging.setShowStackTraces(false);
      reports.getHtml().getRequired().set(true);

      testTask.beforeSuite(closure(prettyLogger::logDescriptors));
      testTask.afterTest(closure(prettyLogger::logResults));
      testTask.afterSuite(closure(prettyLogger::logSummary));
    });
  }
}
