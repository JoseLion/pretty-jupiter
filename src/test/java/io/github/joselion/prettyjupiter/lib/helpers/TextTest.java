package io.github.joselion.prettyjupiter.lib.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import testing.annotations.UnitTest;

@UnitTest public class TextTest {

  @Nested class colored {
    @Nested class when_the_terminal_is_not_dumb {
      @EnumSource(Color.class)
      @ParameterizedTest(name = "[color: {arguments}]")
      void returns_a_colored_text(final Color color) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(false);
          final var text = Text.colored(color, "This is a colored text!");
          final var result = "\u001B[%dmThis is a colored text!\u001B[0m".formatted(color.getCode());

          assertThat(text).isEqualTo(result);
        }
      }
    }

    @Nested class when_the_terminal_is_dumb {
      @EnumSource(Color.class)
      @ParameterizedTest(name = "[color: {arguments}]")
      void returns_the_plain_text(final Color color) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(true);
          final var text = Text.colored(color, "This is a colored text!");

          assertThat(text).isEqualTo("This is a colored text!");
        }
      }
    }
  }

  @Nested class limited {
    static final String LINES = "Multiple lines text\n".repeat(5);

    @Nested class when_the_limit_is_zero_line {
      @Test void returns_only_the_remaining_lines_message() {
        final var limited = Text.limited(LINES, 0);

        assertThat(limited).isEqualTo("--- not showing 5 lines of text ---");
      }
    }

    @Nested class when_the_limit_is_one_line {
      @Test void returns_one_line_and_the_remaining_lines_message() {
        final var limited = Text.limited(LINES, 1);

        assertThat(limited).isEqualTo(
          """
          Multiple lines text
          --- and 4 more ---\
          """
        );
      }
    }

    @Nested class when_the_limit_is_less_than_the_number_of_lines {
      @Test void returns_the_limited_number_of_line_and_the_remaining_lines_message() {
        final var limited = Text.limited(LINES, 3);

        assertThat(limited).isEqualTo(
          """
          Multiple lines text
          Multiple lines text
          Multiple lines text
          --- and 2 more ---\
          """
        );
      }
    }

    @Nested class when_the_limit_is_greater_or_equal_than_the_number_of_lines {
      @ValueSource(ints = {5, 7})
      @ParameterizedTest(name = "[limit: {arguments}]")
      void returns_all_the_line_without_the_remaining_lines_message(final int limit) {
        final var limited = Text.limited(LINES, limit);

        assertThat(limited).isEqualTo(
          """
          Multiple lines text
          Multiple lines text
          Multiple lines text
          Multiple lines text
          Multiple lines text
          """
        );
      }
    }
  }

  @Nested class uncolor {
    @Test void removes_the_color_characters_from_a_colored_text() {
      final var colored = Text.colored(Color.RED, "Hello world!");

      assertThat(Text.uncolored(colored)).isEqualTo("Hello world!");
    }
  }
}
