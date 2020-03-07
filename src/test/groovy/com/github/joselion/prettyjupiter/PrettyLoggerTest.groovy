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

import com.github.joselion.prettyjupiter.helpers.Utils

class PrettyLoggerTest extends Specification {

  def '.logDescriptors'(String className, Integer times) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) {
        getLogger() >> logger
      }
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
      final Project project = Stub(Project) {
        getLogger() >> logger
      }
      final PrettyLogger prettyLogger = new PrettyLogger(project)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> 'This is a test result!'
      }
      final TestResult result = Stub(TestResult) {
        getResultType() >> resultType
        getStartTime() >> 10000
        getEndTime() >> 10500
      }

    when:
      prettyLogger.logResults(descriptor, result)

    then:
      with(logger) {
        1 * lifecycle(log)
      }

    where:
      resultType  | log
      SUCCESS     | "✔ ${ESC}[90mThis is a test result!${ESC}[0m (500ms)"
      FAILURE     | "❌ ${ESC}[31mThis is a test result!${ESC}[0m (500ms)"
      SKIPPED     | "⚠ ${ESC}[33mThis is a test result!${ESC}[0m (500ms)"
  }
}
