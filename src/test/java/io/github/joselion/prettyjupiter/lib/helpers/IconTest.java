package io.github.joselion.prettyjupiter.lib.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import testing.annotations.UnitTest;

@UnitTest class IconTest {

  @Nested class toStrin {
    @Nested class when_the_terminal_is_not_dumb {
      @EnumSource(Icon.class)
      @ParameterizedTest(name = "[icon: {arguments}]")
      void returns_the_text_icon(final Icon icon) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(false);

          assertThat(icon).hasToString(icon.getText());
        }
      }
    }

    @Nested class when_the_terminal_is_dumb {
      @EnumSource(Icon.class)
      @ParameterizedTest(name = "[icon: {arguments}]")
      void returns_the_plain_icon(final Icon icon) {
        try (var mock = mockStatic(Common.class)) {
          mock.when(Common::isTermDumb).thenReturn(true);

          assertThat(icon).hasToString(icon.getPlain());
        }
      }
    }
  }
}
