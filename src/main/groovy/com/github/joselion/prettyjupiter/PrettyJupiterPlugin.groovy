package com.github.joselion.prettyjupiter

import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
import static org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import static org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT

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
    def extension = project.extensions.create('prettyJupiter', PrettyJupiterPluginExtension)

    project.plugins.withType(JavaPlugin) {
      project.tasks.withType(Test) { testTask ->
        def prettyLogger = new PrettyLogger(project, testTask, extension)

        testTask.testLogging {
          events = []
        }

        testTask.reports {
          html.enabled(true)
        }

        testTask.beforeSuite(prettyLogger.&logDescriptors)
        testTask.afterTest(prettyLogger.&logResults)
        testTask.afterSuite(prettyLogger.&logSummary)
      }
    }
  }
}
