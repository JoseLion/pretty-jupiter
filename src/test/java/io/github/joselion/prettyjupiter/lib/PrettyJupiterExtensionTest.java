package io.github.joselion.prettyjupiter.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import testing.annotations.UnitTest;

@UnitTest class PrettyJupiterExtensionTest {

  @Nested class when_the_extension_is_created {
    @Test void assigns_default_values() {
      final var project = ProjectBuilder.builder().build();
      final var extension = project.getExtensions().create("prettyJupiter", PrettyJupiterExtension.class);
      final var duration = extension.duration();
      final var failure = extension.failure();

      assertThat(duration.enabled().get()).isTrue();
      assertThat(duration.threshold().get()).isEqualTo(200);
      assertThat(duration.customThreshold().get()).isEmpty();
      assertThat(failure.maxMessageLines().get()).isEqualTo(15);
      assertThat(failure.maxTraceLines().get()).isEqualTo(15);
    }
  }

  @Nested class threshold {
    @Nested class when_a_custom_threshold_exists {
      @TestFactory Stream<DynamicTest> returns_the_threshold_for_the_specific_test_source() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("prettyJupiter", PrettyJupiterExtension.class);
        final var customThreshold = extension.duration().customThreshold();

        customThreshold.put("test", 100);
        customThreshold.put("e2eTest", 200);
        customThreshold.put("integrationTest", 300);

        return Map.of(
          "task ':test'", 100,
          "task ':app:e2eTest'", 200,
          "task ':lib:app:integrationTest'", 300
        )
        .entrySet()
        .stream()
        .map(entry ->
          dynamicTest("[task log: %s]".formatted(entry.getKey()), () -> {
            final var testTask = mock(org.gradle.api.tasks.testing.Test.class);
            final var duration = extension.duration();

            when(testTask.toString()).thenReturn(entry.getKey());

            assertThat(duration.threshold(testTask)).isEqualTo(entry.getValue());
          })
        );
      }
    }

    @Nested class when_a_custom_threshold_does_not_exists {
      @Test void returns_the_default_threshold() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("prettyJupiter", PrettyJupiterExtension.class);
        final var testTask = mock(org.gradle.api.tasks.testing.Test.class);
        final var duration = extension.duration();

        when(testTask.toString()).thenReturn("task ':integrationTest'");
        duration.customThreshold().put("test", 100);
        duration.threshold().set(150);

        assertThat(duration.threshold(testTask)).isEqualTo(150);
      }
    }
  }
}
