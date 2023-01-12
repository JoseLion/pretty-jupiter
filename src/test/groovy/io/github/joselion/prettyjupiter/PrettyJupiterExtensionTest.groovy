package io.github.joselion.prettyjupiter

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class PrettyJupiterExtensionTest extends Specification {

  def 'creates the extension with default values'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      final PrettyJupiterExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterExtension)

    expect:
      extension.duration.enabled.get() == true
      extension.duration.threshold.get() == 75
      extension.duration.customThreshold.get() == [:]
      extension.failure.maxMessageLines.get() == 15
      extension.failure.maxTraceLines.get() == 10
  }

  def 'can modify extension using assignment'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      final PrettyJupiterExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterExtension)

    when:
      extension.duration.enabled = false
      extension.duration.threshold = 150
      extension.duration.customThreshold.put('integrationTest', 100)
      extension.failure.maxMessageLines = 30
      extension.failure.maxTraceLines = 20

    then:
      extension.duration.enabled.get() == false
      extension.duration.threshold.get() == 150
      extension.duration.customThreshold.get().get('integrationTest') == 100
      extension.failure.maxMessageLines.get() == 30
      extension.failure.maxTraceLines.get() == 20
  }

  def 'can modify extension using closures'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      final PrettyJupiterExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterExtension)

    when:
      extension.duration {
        enabled = false
        threshold = 150
      }

      extension.failure {
        maxMessageLines = 30
        maxTraceLines = 20
      }

    then:
      extension.duration.enabled.get() == false
      extension.duration.threshold.get() == 150
      extension.failure.maxMessageLines.get() == 30
      extension.failure.maxTraceLines.get() == 20
  }

  def 'returns custom threshold when exists'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      final PrettyJupiterExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterExtension)
      final Test testTask = Stub(Test) {
        toString() >> taskLog
      }
      extension.duration.customThreshold.put(testSource, testCustomThreshold)

    when:
      Long duration = extension.getDuration().getThreshold(testTask)

    then:
      duration == testCustomThreshold

    where:
      taskLog                           | testSource        | testCustomThreshold
      "task ':test'"                    | 'test'            | 100
      "task ':app:e2eTest'"             | 'e2eTest'         | 200
      "task ':lib:app:integrationTest'" | 'integrationTest' | 300
  }

  def 'returns default threshold when custom threshold doesnt exist'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      final PrettyJupiterExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterExtension)
      final Test testTask = Stub(Test) {
        toString() >> "task ':integrationTest'"
      }
      extension.duration.customThreshold.put('test', 100)
      def defaultThreshold = 150
      extension.duration.threshold = defaultThreshold

    when:
      Long duration = extension.getDuration().getThreshold(testTask)

    then:
      duration == defaultThreshold
  }
}
