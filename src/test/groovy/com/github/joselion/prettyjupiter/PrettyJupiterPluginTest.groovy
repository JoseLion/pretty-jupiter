package com.github.joselion.prettyjupiter

import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification


class PrettyJupiterPluginTest extends Specification {

  def 'plugin applied when test task exist'(String basePlugin) {
    given:
      final Project project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('com.github.joselion.pretty-jupiter')
      project.plugins.apply(basePlugin)

    then:
      project.test.testLogging.events as Set == [TestLogEvent.STANDARD_ERROR] as Set
      project.test.testLogging.showExceptions == false
      project.test.testLogging.showStackTraces == false
      project.test.reports.html.enabled == true

    where:
      basePlugin << ['java', 'java-library', 'groovy', 'java-gradle-plugin']
  }

  def 'plugin applied when test task does NOT exist'() {
    given:
      final Project project = ProjectBuilder.builder().build()
      project.plugins.apply('com.github.joselion.pretty-jupiter')

    when:
      project.test

    then:
      MissingPropertyException exception = thrown()
      exception.message == "Could not get unknown property 'test' for root project 'test' of type org.gradle.api.Project."
  }
}
