package io.github.joselion.prettyjupiter;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.internal.impldep.org.junit.Test;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import testing.annotations.UnitTest;

@UnitTest class PrettyJupiterPluginTest {

  @Nested class when_a_test_task_exists {
    @ParameterizedTest(name = "[plugin: {arguments}]")
    @ValueSource(strings = {"java", "java-library", "groovy", "java-gradle-plugin"})
    void the_plugin_is_applied(final String basePlugin) {
      final var project = ProjectBuilder.builder().build();
      project.getPlugins().apply("io.github.joselion.pretty-jupiter");
      project.getPlugins().apply(basePlugin);

      final var testTask = project
        .getTasks()
        .withType(org.gradle.api.tasks.testing.Test.class)
        .getByName("test");

      assertThat(testTask.getTestLogging().getEvents()).containsOnly(TestLogEvent.STANDARD_ERROR);
      assertThat(testTask.getTestLogging().getShowExceptions()).isFalse();
      assertThat(testTask.getTestLogging().getShowStackTraces()).isFalse();
      assertThat(testTask.getReports().getHtml().getRequired().get()).isTrue();
    }
  }

  @Nested class when_a_test_task_does_not_exists {
    @Test void the_plugin_is_applied_and_no_test_task_is_found() {
      final var project = ProjectBuilder.builder().build();
      project.getPlugins().apply("io.github.joselion.pretty-jupiter");

      final var testTask = project
        .getTasks()
        .withType(org.gradle.api.tasks.testing.Test.class)
        .getByName("test");

      assertThat(testTask).isNull();
    }
  }
}
