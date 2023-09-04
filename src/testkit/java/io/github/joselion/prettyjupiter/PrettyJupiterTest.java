package io.github.joselion.prettyjupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.annotations.TestkitTest;

@TestkitTest class PrettyJupiterTest {

  private static final File PROJECT_DIR = new File("build/e2e");

  @BeforeAll static void setup() throws IOException {
    PROJECT_DIR.mkdirs();

    try (var writer = new BufferedWriter(new FileWriter("build/e2e/settings.gradle"))) {
      writer.write("");
    }
  }

  @Nested class when_the_plugin_is_applied {
    @Test void runs_test_task_successfully() {
      writeBuildGradle("""
        plugins {
          id('java')
          id('io.github.joselion.pretty-jupiter')
        }
        """);

      final var result = runTask("test");

      assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }
  }

  @Nested class when_the_project_has_more_than_one_test_source {
    @Test void applies_the_plugin_to_all_test_sources() {
      writeBuildGradle("""
        plugins {
          id('java')
          id('io.github.joselion.pretty-jupiter')
        }

        sourceSets {
          e2e {
          }
        }

        tasks.register('e2e', Test) {
          testClassesDirs = sourceSets.e2e.output.classesDirs
          classpath = sourceSets.e2e.runtimeClasspath
        }

        tasks.named('test') {
          finalizedBy(tasks.e2e)
        }
      """);

      final var result = runTask("test");

      assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }
  }

  @Nested class when_the_plugin_is_configured {
    @Nested class and_an_extension_property_is_changed {
      @Test void updates_the_plugin_configuration() {
        writeBuildGradle("""
          plugins {
            id('java')
            id('io.github.joselion.pretty-jupiter')
          }

          prettyJupiter {
            duration.threshold = 500
          }

          task showThreshold() {
            doLast {
              println("*** duration.threshold: ${prettyJupiter.duration.threshold.get()}")
            }
          }
        """);

        final var result = runTask("showThreshold");

        assertThat(result.getOutput())
          .contains("*** duration.threshold: 500")
          .contains("BUILD SUCCESSFUL");
      }
    }

    @Nested class and_closures_are_used_to_change_properties {
      @Test void updates_the_plugin_configuration() {
        writeBuildGradle("""
          plugins {
            id('java')
            id('io.github.joselion.pretty-jupiter')
          }

          prettyJupiter {
            duration {
              enabled = false
              threshold = 500
              customThreshold = [test : 100, integrationTest : 200]
            }

            failure {
              maxMessageLines = 25
              maxTraceLines = 150
            }

            task showConfig() {
              doLast {
                println("*** duration.enabled: ${prettyJupiter.duration.enabled.get()}")
                println("*** duration.threshold: ${prettyJupiter.duration.threshold.get()}")
                println("*** duration.customThreshold: ${prettyJupiter.duration.customThreshold.get()}")
                println("*** failure.maxMessageLines: ${prettyJupiter.failure.maxMessageLines.get()}")
                println("*** failure.maxTraceLines: ${prettyJupiter.failure.maxTraceLines.get()}")
              }
            }
          }
        """);

        final var result = runTask("showConfig");

        assertThat(result.getOutput())
          .contains("duration.enabled: false")
          .contains("duration.threshold: 500")
          .contains("duration.customThreshold: [test:100, integrationTest:200]")
          .contains("failure.maxMessageLines: 25")
          .contains("failure.maxTraceLines: 150")
          .contains("BUILD SUCCESSFUL");
      }
    }
  }

  private void writeBuildGradle(final String text) {
    try (var writer = new BufferedWriter(new FileWriter("build/e2e/build.gradle"))) {
      writer.write(text);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create build.gradle file", e);
    }
  }

  private BuildResult runTask(final String... arguments) {
    final var runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments(arguments);
    runner.withProjectDir(PROJECT_DIR);

    return runner.build();
  }
}
