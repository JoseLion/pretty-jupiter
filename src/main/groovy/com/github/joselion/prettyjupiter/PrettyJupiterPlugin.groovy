package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
import static org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test

/**
 * A Gradle plugin to better log JUnit Jupiter tests when run from the Gradle
 * test task. Nested tests are grouped and the output styles is very similar to
 * {@code mocha.js}
 */
public class PrettyJupiterPlugin implements Plugin<Project> {
  public void apply(Project project) {
    project.plugins.withType(JavaPlugin) {
      project.tasks.withType(Test) { testTaks ->
        final PrettyJupiterPluginExtension extension = project.extensions.create('prettyJupiter', PrettyJupiterPluginExtension)
        final PrettyLogger prettyLogger = new PrettyLogger(project, extension)

        testTaks.testLogging {
          events = [STANDARD_ERROR]
          exceptionFormat(SHORT)
          showStandardStreams(true)
        }

        testTaks.beforeSuite(prettyLogger.&logDescriptors)
        testTaks.afterTest(prettyLogger.&logResults)
        testTaks.afterSuite(prettyLogger.&logSummary)
      }
    }
  }
}
