package com.github.joselion.prettyjupiter

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC
import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SKIPPED

import spock.lang.Specification
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType
import org.gradle.testfixtures.ProjectBuilder

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

class PrettyLoggerTest extends Specification {

  def '.logDescriptors'(String className, Integer times) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final PrettyLogger prettyLogger = new PrettyLogger(project)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getClassName() >> className
        getDisplayName() >> 'This is a test description!'
      }

    when:
      prettyLogger.logDescriptors(descriptor)

    then:
      with(logger) {
        times * lifecycle("${ESC}[97mThis is a test description!${ESC}[0m")
      }

    where:
      className | times
      null      | 0
      'any'     | 1
  }

  def '.logResults'(ResultType resultType, String log) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final PrettyLogger prettyLogger = new PrettyLogger(project)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> 'This is a test result!'
      }
      final TestResult result = Stub(TestResult) {
        getResultType() >> resultType
        getStartTime() >> 10000
        getEndTime() >> 10010
      }

    when:
      prettyLogger.logResults(descriptor, result)

    then:
      with(logger) {
        1 * lifecycle(log)
      }

    where:
      resultType  | log
      SUCCESS     | "✔ ${ESC}[90mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
      FAILURE     | "❌ ${ESC}[31mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
      SKIPPED     | "⚠ ${ESC}[33mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
  }

  def 'duration colors'(Long startTime, Long endTime, Colors color) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final PrettyJupiterPluginExtension extension = new PrettyJupiterPluginExtension()
      final PrettyLogger prettyLogger = new PrettyLogger(project, extension)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> 'Another test description comes here'
      }
      final TestResult result = Stub(TestResult) {
        getResultType() >> SUCCESS
        getStartTime() >> startTime
        getEndTime() >> endTime
      }

      when:
        prettyLogger.logResults(descriptor, result)

      then:
        with(logger) {
          final int colorCode = color.getCode()
          final long diff = endTime - startTime
          1 * lifecycle("✔ ${ESC}[90mAnother test description comes here${ESC}[0m (${ESC}[${colorCode}m${diff}ms${ESC}[0m)")
        }

      where:
        startTime | endTime | color
        0         | 100     | Colors.RED
        0         | 75      | Colors.RED
        0         | 50      | Colors.YELLOW
        0         | 38      | Colors.YELLOW
        0         | 25      | Colors.WHITE
        0         | 0       | Colors.WHITE
  }

  def 'disable duration'() {
    final Logger logger = Mock()
    final Project project = Stub(Project) { getLogger() >> logger }
    final PrettyJupiterPluginExtension extension = new PrettyJupiterPluginExtension()
    extension.duration.enabled = false
    final PrettyLogger prettyLogger = new PrettyLogger(project, extension)
    final TestDescriptor descriptor = Stub(TestDescriptor) {
      getParent() >> null
      getDisplayName() >> 'Some tests without duration'
    }
    final TestResult result = Stub(TestResult) {
      getResultType() >> SUCCESS
      getStartTime() >> 100
      getEndTime() >> 500
    }

    when:
      prettyLogger.logResults(descriptor, result)

    then:
      with(logger) {
        1 * lifecycle("✔ ${ESC}[90mSome tests without duration${ESC}[0m")
      }
  }
}
