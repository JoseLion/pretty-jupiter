package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT

import groovy.lang.MissingPropertyException
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification


class PrettyJupiterPluginTest extends Specification {

  def 'plugin applied when test task exist'(String basePlugin) {
    given:
      final Project project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('com.github.joselion.pretty-jupiter')
      project.plugins.apply(basePlugin)

    then:
      project.test.testLogging.exceptionFormat == SHORT
      project.test.testLogging.showStandardStreams == true

    where:
      basePlugin << ['java', 'java-library', 'groovy', 'java-gradle-plugin']
  }

  def 'plugin applied when test task *does not* exist'() {
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
