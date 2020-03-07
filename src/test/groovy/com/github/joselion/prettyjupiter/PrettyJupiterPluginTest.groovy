package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import spock.lang.Specification

import com.github.joselion.prettyjupiter.helpers.PrettyJupiterPluginException


class PrettyJupiterPluginTest extends Specification {

  def 'plugin applied when test task exist'() {
    given:
      final Project project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply('com.github.joselion.pretty-jupiter')

    then:
      project.test.testLogging.exceptionFormat == SHORT
      project.test.testLogging.showStandardStreams == true
  }

  def 'plugin applied when test task *does not* exist'() {
    given:
      final Project project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('com.github.joselion.pretty-jupiter')

    then:
      PluginApplicationException exception = thrown()
      exception.cause instanceof PrettyJupiterPluginException
      exception.cause.message == 'This plugin canm only be applied if a test task exist!'
  }
}
