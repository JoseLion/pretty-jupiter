package com.github.joselion.prettyjupiter

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class FailureTest extends Specification {

  private static final ObjectFactory objects = ProjectBuilder.builder().build().objects
  private static final PrettyJupiterExtension defaultConfig = objects.newInstance(PrettyJupiterExtension)
  private static final Exception causeD = new Exception('Cause of error C')
  private static final Exception causeC = new Exception('Cause of error B', causeD)
  private static final Exception causeB = new Exception('Cause of error A', causeC)
  private static final AssertionError causeA = new AssertionError('Cause of top error', causeB)
  private static final Exception fullCause = new Exception('Top error', causeA)

  def '.cause'(Throwable topError, String result) {
    given:
      final Failure failure = new Failure(topError, desc(1), defaultConfig)

    expect:
      failure.getCause() == result

    where:
      topError        | result
      new Exception() | null
      causeC          | "${ESC}[33m+ java.lang.Exception: Cause of error C${ESC}[0m"
      causeB          | "${ESC}[33m+ java.lang.Exception: Cause of error B\n" +
                                  "└─── java.lang.Exception: Cause of error C${ESC}[0m"
      causeA          | "${ESC}[33m+ java.lang.Exception: Cause of error A\n" +
                                  '└─┬─ java.lang.Exception: Cause of error B\n' +
                                  "  └─── java.lang.Exception: Cause of error C${ESC}[0m"
      fullCause       | "${ESC}[33m+ java.lang.AssertionError: Cause of top error\n" +
                                  '└─┬─ java.lang.Exception: Cause of error A\n' +
                                  '  └─┬─ java.lang.Exception: Cause of error B\n' +
                                  "    └─── java.lang.Exception: Cause of error C${ESC}[0m"
  }

  def '.location'(TestDescriptor descriptor, String location) {
    given:
      final Failure failure = new Failure(new Exception(), descriptor, defaultConfig)

    expect:
      failure.getLocation() == location

    where:
      descriptor  | location
      desc()      | ''
      desc(1)     | 'Test description 1'
      desc(3)     | 'Test description 1 => Test description 2 => Test description 3'
  }

  def '.message'() {
    given:
      final String error = 'This should be an Assertion error!'
      final Failure failure = new Failure(new Exception(error), desc(1), defaultConfig)

    expect:
      failure.getMessage() == "${ESC}[91mjava.lang.Exception: ${error}${ESC}[0m"
  }

  def '.trace'() {
    given:
      final Exception exception = new Exception('Some error message')
      final Failure failure = new Failure(exception, desc(1), defaultConfig)

    expect:
      final Integer maxTrace = defaultConfig.failure.maxTraceLines.get()
      final Integer traceDiff = exception.getStackTrace().length - maxTrace
      final List<String> lines = failure.getTrace().readLines()

      lines.size() == maxTrace + 2
      lines.first() == "${ESC}[90mjava.lang.Exception: Some error message"
      lines.last() == "  --- and ${traceDiff} more ---${ESC}[0m"

      lines.eachWithIndex { line, count ->
        assert line.startsWith('  ') == (count > 0)
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
        getDisplayName() >> "Test description ${num - 1}"
      }
    }

    return Stub(TestDescriptor) {
      getParent() >> descriptorWithParents(num - 1)
      getDisplayName() >> "Test description ${num - 1}"
    }
  }
}
