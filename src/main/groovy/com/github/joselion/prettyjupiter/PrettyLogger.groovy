package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SKIPPED

import groovy.time.TimeDuration
import groovy.time.TimeCategory
import java.lang.Throwable
import java.util.List
import org.gradle.api.Project
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

public class PrettyLogger {

  private final def statusMap = [
    (SUCCESS): [icon: '✔', color: Colors.GRAY],
    (FAILURE): [icon: '❌', color: Colors.RED],
    (SKIPPED): [icon: '⚠', color: Colors.YELLOW]
  ]

  private Project project

  private PrettyJupiterPluginExtension extension

  private List<Failure> failures;

  public PrettyLogger(Project project, PrettyJupiterPluginExtension extension = new PrettyJupiterPluginExtension()) {
    this.project = project
    this.extension = extension
    this.failures = []
  }

  public void logDescriptors(TestDescriptor descriptor) {
    if (descriptor.getClassName() != null) {
      final String tabs = Utils.getTabs(descriptor)
      def desc = Utils.coloredText(Colors.WHITE, descriptor.getDisplayName())

      project.logger.lifecycle("${tabs}${desc}")
    }
  }

  public void logResults(TestDescriptor descriptor, TestResult result) {
    final def status = statusMap[result.getResultType()]
    final String tabs = Utils.getTabs(descriptor)
    final String desc = Utils.coloredText(status.color, descriptor.getDisplayName())
    final String duration = getDuration(result)

    if (result.getResultType() == FAILURE && result.getException() != null) {
      this.failures.add(new Failure(result.getException(), descriptor, extension))
    }

    project.logger.lifecycle("${tabs}${status.icon} ${desc}${duration}")
  }

  public void logSummary(TestDescriptor descriptor, TestResult result) {
    if (!descriptor.parent) {
      final def status = statusMap[result.getResultType()]
      final String symbol = '*'
      final String successes = Utils.coloredText(Colors.GREEN, "${result.successfulTestCount} successes")
      final String failures = Utils.coloredText(Colors.RED, "${result.failedTestCount} failures")
      final String skipped = Utils.coloredText(Colors.YELLOW, "${result.skippedTestCount} skipped")
      final TimeDuration time = TimeCategory.minus(new Date(result.endTime), new Date(result.startTime))
      final String summary = "${symbol} ${status.icon} ${result.testCount} tests completed, ${successes}, ${failures}, ${skipped} (${time}) ${symbol}"
      final String rawText = "${symbol} ${status.icon} ${result.testCount} tests completed, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped (${time}) ${symbol}"
      final String border = symbol * rawText.length()
      
      project.logger.lifecycle('\n\n')

      this.failures.eachWithIndex { failure, i ->
        final String n = Utils.coloredText(Colors.BRIGHT_RED, "(${i + 1})");
        final String ns = ' ' * ("${i}".length() + 2)

        project.logger.lifecycle("${n}  ${failure.getLocation()}:")
        failure.getMessage().eachLine {
          project.logger.lifecycle("${ns}    ${it}")
        }
        
        project.logger.lifecycle('')
        project.logger.lifecycle("${ns}  Failure stack trace:")
        failure.getTrace().eachLine {
          project.logger.lifecycle("${ns}    ${it}")
        }
        project.logger.lifecycle('\n')
      }

      project.logger.lifecycle('\n')
      project.logger.lifecycle("${border}")
      project.logger.lifecycle(summary)
      project.logger.lifecycle("${border}")
      
    }
  }

  private String getDuration(TestResult result) {
    if (extension.duration.enabled) {
      final long timeDiff = result.getEndTime() - result.getStartTime()
      final Colors color = timeDiff >= extension.duration.threshold
        ? Colors.RED
        : timeDiff >= extension.duration.threshold / 2
          ? Colors.YELLOW
          : Colors.WHITE
      final String millis = Utils.coloredText(color, "${timeDiff}ms")

      return " (${millis})"
    }

    return ''
  }
  
}