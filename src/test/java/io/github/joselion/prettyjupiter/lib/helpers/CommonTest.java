package io.github.joselion.prettyjupiter.lib.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import testing.Helpers;
import testing.Spy;

public class CommonTest {

  @Nested class closure {
    @Nested class when_the_argument_is_a_Consumer {
      @Test void creates_a_closure_that_runs_the_Consumer_on_the_doCall_method() {
        final var consumerSpy = Spy.<Consumer<String>>lambda(x -> { });
        final var closure = Common.closure(consumerSpy);

        closure.call("foo");

        verify(consumerSpy, only()).accept("foo");
      }
    }

    @Nested class when_the_argument_is_a_BiConsumer {
      @Test void creates_a_closure_that_runs_the_BiConsumer_on_the_doCall_method() {
        final var biConsumerSpy = Spy.<BiConsumer<String, Integer>>lambda((x, y) -> { });
        final var closure = Common.closure(biConsumerSpy);

        closure.call("bar", 5);

        verify(biConsumerSpy, only()).accept("bar", 5);
      }
    }
  }

  @Nested class levelFor {
    @Test void returns_the_effective_level_of_parents_of_a_test_descriptor() {
      final var descriptor = Helpers.descriptorOf(7);

      assertThat(Common.levelFor(descriptor)).isEqualTo(5);
    }
  }

  @Nested class tabsFor {
    @TestFactory Stream<DynamicTest> returns_the_tabs_based_on_the_descriptor_level() {
      return Stream.<MapEntry<Integer, String>>of(
        entry(null, ""),
        entry(0, ""),
        entry(1, ""),
        entry(2, ""),
        entry(3, "  "),
        entry(4, "    "),
        entry(5, "      ")
      )
      .map(entry ->
        dynamicTest("[level: %d]".formatted(entry.getKey()), () -> {
          final var descriptor = Helpers.descriptorOf(entry.getKey());

          assertThat(Common.tabsFor(descriptor)).isEqualTo(entry.getValue());
        })
      );
    }
  }
}
