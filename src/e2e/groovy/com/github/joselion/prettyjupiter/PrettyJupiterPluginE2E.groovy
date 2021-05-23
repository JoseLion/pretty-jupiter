package com.github.joselion.prettyjupiter

import spock.lang.Specification

import org.gradle.testkit.runner.GradleRunner

class PrettyJupiterPluginE2E extends Specification {

  def 'can apply the plugin'() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
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
        runner.withArguments('test')
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains('BUILD SUCCESSFUL')
  }

  def 'can apply the plugin with 2 test tasks'() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.pretty-jupiter')
        |}
        |
        |sourceSets {
        |  e2e {
        |  }
        |}
        |
        |tasks.register('e2e', Test) {
        |  testClassesDirs = sourceSets.e2e.output.classesDirs
        |  classpath = sourceSets.e2e.runtimeClasspath
        |}
        |
        |tasks.named('test') {
        |  finalizedBy(tasks.e2e)
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments('test')
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains('BUILD SUCCESSFUL')
  }

  def 'can update the extension properties'() {
    def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.pretty-jupiter')
        |}
        |
        |prettyJupiter {
        |  duration.threshold = 500
        |}
        |
        |task showThreshold() {
        |  print('THRESHOLD: ' + prettyJupiter.duration.threshold.get())
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments('test')
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains('THRESHOLD: 500')
  }

  def 'can use closures to configure the extension'() {
    def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.pretty-jupiter')
        |}
        |
        |prettyJupiter {
        |  duration {
        |    enabled = false
        |    threshold = 500
        |    customThreshold = [test : 100, integrationTest : 200]
        |  }
        |
        |  failure {
        |    maxMessageLines = 25
        |    maxTraceLines = 150
        |  }
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments('test')
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains('BUILD SUCCESSFUL')
  }

  def 'can update the extension customThreshold properties'() {
    def projectDir = new File('build/e2e')
    projectDir.mkdirs()
    new File(projectDir, 'settings.gradle') << ''
    def buildGradle = new File(projectDir, 'build.gradle')
    buildGradle.bytes = []
    buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.pretty-jupiter')
        |}
        |
        |prettyJupiter {
        |  duration.customThreshold = [test : 100, integrationTest : 200]
        |}
        |
        |task showThreshold() {
        |  print('Test threshold: ' + prettyJupiter.duration.customThreshold.get().get('test'))
        |  print('Integration test threshold: ' + prettyJupiter.duration.customThreshold.get().get('integrationTest'))
        |}
      |"""
            .stripMargin()

    when:
    def runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments('test')
    runner.withProjectDir(projectDir)
    def result = runner.build()

    then:
    result.output.contains('Test threshold: 100')
    result.output.contains('Integration test threshold: 200')
  }
}
