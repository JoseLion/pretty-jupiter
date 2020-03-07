package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SKIPPED

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

  public PrettyLogger(Project project) {
    this.project = project
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
    final long timeDiff = result.getEndTime() - result.getStartTime()
    final String desc = Utils.coloredText(status.color, descriptor.getDisplayName())

    project.logger.lifecycle("${tabs}${status.icon} ${desc} (${timeDiff}ms)")
  }
  
}