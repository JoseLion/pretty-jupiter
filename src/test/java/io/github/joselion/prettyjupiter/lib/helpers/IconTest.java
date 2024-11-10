package io.github.joselion.prettyjupiter.lib.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import testing.annotations.UnitTest;

@UnitTest class IconTest {

  @Nested class toString {
    @Nested class when_the_terminal_is_not_dumb {
      @EnumSource(Icon.class)
      @ParameterizedTest(name = "[icon: {arguments}]")
      void returns_the_colored_text_icon(final Icon icon) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(false);
          final var colored = Text.colored(icon.color(), icon.text());

          assertThat(icon).hasToString(colored);
        }
      }
    }

    @Nested class when_the_terminal_is_dumb {
      @EnumSource(Icon.class)
      @ParameterizedTest(name = "[icon: {arguments}]")
      void returns_the_uncolored_icon(final Icon icon) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(true);

          assertThat(icon).hasToString(icon.text());
        }
      }
    }
  }
}
