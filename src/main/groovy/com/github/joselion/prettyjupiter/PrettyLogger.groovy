package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SKIPPED
import static com.github.joselion.prettyjupiter.helpers.Utils.ESC

import groovy.time.TimeDuration
import groovy.time.TimeCategory
import java.lang.Throwable
import java.util.List
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Icons
import com.github.joselion.prettyjupiter.helpers.Utils

public class PrettyLogger {

  private final def statusMap = [
    (SUCCESS): [icon: Icons.SUCCESS, color: Colors.GRAY],
    (FAILURE): [icon: Icons.FAILURE, color: Colors.RED],
    (SKIPPED): [icon: Icons.SKIPPED, color: Colors.YELLOW]
  ]

  private Project project

  private Test testTask

  private PrettyJupiterPluginExtension extension

  private List<Failure> failures;

  public PrettyLogger(Project project, Test testTask, PrettyJupiterPluginExtension extension = new PrettyJupiterPluginExtension()) {
    this.project = project
    this.testTask = testTask
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
      project.logger.lifecycle('\n\n')

      this.failures.eachWithIndex { failure, i ->
        final String n = Utils.coloredText(Colors.BRIGHT_RED, "(${i + 1})");
        final String ns = ' ' * ("${i}".length() + 2)

        project.logger.lifecycle("${n}  ${failure.getLocation()}:")
        failure.getMessage().eachLine {
          project.logger.lifecycle("${ns}    ${it}")
        }

        if (failure.getCause()) {
          project.logger.lifecycle('')
          project.logger.lifecycle("${ns}  Caused by:")
          failure.getCause().eachLine {
            project.logger.lifecycle("${ns}    ${it}")
          }
        }
        
        project.logger.lifecycle('')
        project.logger.lifecycle("${ns}  Stack trace:")
        failure.getTrace().eachLine {
          project.logger.lifecycle("${ns}    ${it}")
        }
        project.logger.lifecycle('\n')
      }

      final def status = statusMap[result.getResultType()]
      final String successes = Utils.coloredText(Colors.GREEN, "${result.successfulTestCount} successes")
      final String failures = Utils.coloredText(Colors.RED, "${result.failedTestCount} failures")
      final String skipped = Utils.coloredText(Colors.YELLOW, "${result.skippedTestCount} skipped")
      final TimeDuration time = TimeCategory.minus(new Date(result.endTime), new Date(result.startTime))
      final String stats = "${status.icon} ${result.testCount} tests completed, ${successes}, ${failures}, ${skipped} (${time})"
      final String report = "Report: ${this.testTask.reports.html.entryPoint}"
      final String summary = [stats, '', report].join('\n')
      def normalize = { String s ->
        s.replace("${ESC}", '')
          .replaceAll(/\[\d*m/, '')
      }

      final Integer max = summary.lines()
        .map(normalize)
        .max({ a, b -> a.length() - b.length() })
        .orElse("")
        .length() + 1

      project.logger.lifecycle('\n')
      project.logger.lifecycle('┌─' + ('─' * max) + '─┐')
      summary.lines().toArray().each {
        final int end = Icons.values()*.icon.any { icon -> it.contains(icon) } ? 1 : 0
        final String ws = ' ' * (max - normalize(it).length() - end)
        project.logger.lifecycle("| ${it}${ws} |")
      }
      project.logger.lifecycle('└─' + ('─' * max) + '─┘')
      
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