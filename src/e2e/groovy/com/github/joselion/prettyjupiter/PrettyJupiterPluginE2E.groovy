package com.github.joselion.prettyjupiter

import spock.lang.Specification

import org.gradle.testkit.runner.GradleRunner

class PrettyJupiterPluginE2E extends Specification {

  def 'can apply the plugin'() {
    given:
      def projectDir = new File("build/e2e")
      projectDir.mkdirs()
      new File(projectDir, "settings.gradle") << ""
      def buildGradle = new File(projectDir, "build.gradle")
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.pretty-jupiter')
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("test")
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains('BUILD SUCCESSFUL')
  }
}
