package com.github.joselion.prettyjupiter

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC
import static org.gradle.api.tasks.testing.TestResult.ResultType.FAILURE
import static org.gradle.api.tasks.testing.TestResult.ResultType.SKIPPED
import static org.gradle.api.tasks.testing.TestResult.ResultType.SUCCESS

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Icons

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType
import org.gradle.api.tasks.testing.TestTaskReports
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class PrettyLoggerTest extends Specification {

  private static final ObjectFactory objects = ProjectBuilder.builder().build().objects

  def '.logDescriptors'(String className, Integer times) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final Test testTask = Stub(Test)
      final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
      final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
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
      final Test testTask = Stub(Test)
      final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
      final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> 'This is a test result!'
      }
      final TestResult results = Stub(TestResult) {
        getResultType() >> resultType
        getStartTime() >> 10000
        getEndTime() >> 10010
        getException() >> null
      }

    when:
      prettyLogger.logResults(descriptor, results)

    then:
      with(logger) {
        1 * lifecycle(log)
      }

    where:
      resultType  | log
      SUCCESS     | "${Icons.SUCCESS} ${ESC}[90mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
      FAILURE     | "${Icons.FAILURE} ${ESC}[31mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
      SKIPPED     | "${Icons.SKIPPED} ${ESC}[33mThis is a test result!${ESC}[0m (${ESC}[97m10ms${ESC}[0m)"
  }

  def '.logSummary (not yet in summary)'() {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final Test testTask = Stub(Test)
      final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
      final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
      final TestDescriptor descriptor = desc(1)
      final TestResult results = Stub(TestResult)

    when:
      prettyLogger.logSummary(descriptor, results)

    then:
      with(logger) {
        0 * lifecycle(_)
      }
  }

  def '.logSummary (in summary)'(ResultType resultType, Icons icon) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final Test testTask = Stub(Test) {
        getReports() >> Stub(TestTaskReports) {
          getHtml() >> Stub(DirectoryReport) {
            getEntryPoint() >> Stub(File) {
              toString() >> 'path/to/report/file.html'
            }
          }
        }
      }
      final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
      final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
      final TestDescriptor descriptor = Stub(TestDescriptor) { getParent() >> null }
      final Exception causeD = new Exception('Cause of error C')
      final Exception causeC = new Exception('Cause of error B', causeD)
      final Exception causeB = new Exception('Cause of error A', causeC)
      final Exception causeA = new Exception('Cause of top error', causeB)
      final Exception exception = new Exception('\nMulti\nline\nexception!', causeA)
      final TestResult testRes1 = Stub(TestResult) {
        getResultType() >> FAILURE
        getException() >> exception
      }
      final TestResult testRes2 = Stub(TestResult) {
        getResultType() >> FAILURE
        getException() >> causeD
      }
      final TestResult results = Stub(TestResult) {
        getResultType() >> resultType
        getStartTime() >> 1583909261673
        getEndTime() >> 1583909305290
        getTestCount() >> 136
        getSuccessfulTestCount() >> 120
        getFailedTestCount() >> 10
        getSkippedTestCount() >> 6
      }

    when:
      prettyLogger.logResults(desc(1), testRes1)
      prettyLogger.logResults(desc(1), testRes2)
      prettyLogger.logSummary(descriptor, results)

    then:
      with(logger) {
        def normalize = { String s ->
          s.replace("${ESC}", '')
            .replaceAll(/\[\d*m/, '')
        }
        final String rawText = "${icon} 136 tests completed, ${ESC}[32m120 successes${ESC}[0m, ${ESC}[31m10 failures${ESC}[0m, ${ESC}[33m6 skipped${ESC}[0m (43.617 seconds)"
        final String visibleText = normalize(rawText)
        final String rawReport = 'Report: path/to/report/file.html'

        1 * lifecycle('\n\n')
        1 * lifecycle("${ESC}[91m(1)${ESC}[0m  Test 1:")
        1 * lifecycle("       ${ESC}[91mjava.lang.Exception: ")
        1 * lifecycle('       Multi')
        1 * lifecycle('       line')
        1 * lifecycle("       exception!${ESC}[0m")
        1 * lifecycle('')
        1 * lifecycle('     Caused by:')
        1 * lifecycle("       ${ESC}[33m+ java.lang.Exception: Cause of top error")
        1 * lifecycle('       └─┬─ java.lang.Exception: Cause of error A')
        1 * lifecycle('         └─┬─ java.lang.Exception: Cause of error B')
        1 * lifecycle("           └─── java.lang.Exception: Cause of error C${ESC}[0m")
        1 * lifecycle('')
        1 * lifecycle('     Stack trace:')
        1 * lifecycle("       ${ESC}[90mjava.lang.Exception:  Multi line exception!")
        1 * lifecycle("         at ${exception.getStackTrace()[0]}")
        1 * lifecycle("         at ${exception.getStackTrace()[1]}")
        1 * lifecycle("         at ${exception.getStackTrace()[2]}")
        1 * lifecycle("         at ${exception.getStackTrace()[3]}")
        1 * lifecycle("         at ${exception.getStackTrace()[4]}")
        1 * lifecycle("         at ${exception.getStackTrace()[5]}")
        1 * lifecycle("         at ${exception.getStackTrace()[6]}")
        1 * lifecycle("         at ${exception.getStackTrace()[7]}")
        1 * lifecycle("         at ${exception.getStackTrace()[8]}")
        1 * lifecycle("         at ${exception.getStackTrace()[9]}")
        1 * lifecycle("         --- and ${exception.getStackTrace().length - 10} more ---${ESC}[0m")
        1 * lifecycle('\n')
        1 * lifecycle("${ESC}[91m(2)${ESC}[0m  Test 1:")
        1 * lifecycle("       ${ESC}[91mjava.lang.Exception: Cause of error C${ESC}[0m")
        1 * lifecycle('')
        1 * lifecycle('     Stack trace:')
        1 * lifecycle("       ${ESC}[90mjava.lang.Exception: Cause of error C")
        1 * lifecycle("         at ${causeD.getStackTrace()[0]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[1]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[2]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[3]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[4]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[5]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[6]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[7]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[8]}")
        1 * lifecycle("         at ${causeD.getStackTrace()[9]}")
        1 * lifecycle("         --- and ${causeD.getStackTrace().length - 10} more ---${ESC}[0m")
        2 * lifecycle('\n')
        1 * lifecycle('┌─' + '─' * visibleText.length() + '──┐')
        1 * lifecycle("| ${rawText}  |")
        1 * lifecycle('| ' + ' ' * visibleText.length() + '  |')
        1 * lifecycle('| ' + rawReport + ' ' * (visibleText.length() - rawReport.length()) + '  |')
        1 * lifecycle('└─' + '─' * visibleText.length() + '──┘')
      }

    where:
      resultType  | icon
      SUCCESS     | Icons.SUCCESS
      FAILURE     | Icons.FAILURE
      SKIPPED     | Icons.SKIPPED
  }

  def 'duration colors'(Long startTime, Long endTime, Colors color) {
    given:
      final Logger logger = Mock()
      final Project project = Stub(Project) { getLogger() >> logger }
      final Test testTask = Stub(Test)
      final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
      final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
      final TestDescriptor descriptor = Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> 'Another test description comes here'
      }
      final TestResult results = Stub(TestResult) {
        getResultType() >> SUCCESS
        getStartTime() >> startTime
        getEndTime() >> endTime
      }

      when:
        prettyLogger.logResults(descriptor, results)

      then:
        with(logger) {
          final int colorCode = color.getCode()
          final long diff = endTime - startTime
          1 * lifecycle("${Icons.SUCCESS} ${ESC}[90mAnother test description comes here${ESC}[0m (${ESC}[${colorCode}m${diff}ms${ESC}[0m)")
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
    final Test testTask = Stub(Test)
    final PrettyJupiterExtension extension = objects.newInstance(PrettyJupiterExtension)
    extension.duration.enabled.set(false)
    final PrettyLogger prettyLogger = new PrettyLogger(project, testTask, extension)
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
        1 * lifecycle("${Icons.SUCCESS} ${ESC}[90mSome tests without duration${ESC}[0m")
      }
  }

  private TestDescriptor desc(Integer parents = 0) {
    final Integer num = parents + 1

    return Stub(TestDescriptor) {
      getParent() >> Stub(TestDescriptor) {
        getParent() >> descriptorWithParents(num)
      }
    }
  }

  private TestDescriptor descriptorWithParents(Integer num) {
    if (num == null) {
      return null
    }

    if (num == 0) {
      return Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> "Test ${num - 1}"
      }
    }

    return Stub(TestDescriptor) {
      getParent() >> descriptorWithParents(num - 1)
      getDisplayName() >> "Test ${num - 1}"
    }
  }
}
