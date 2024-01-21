package io.github.joselion.prettyjupiter.lib.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Color {
  BRIGHT_RED(91),
  GRAY(90),
  GREEN(32),
  RED(31),
  YELLOW(33);

  private final int code;

  public String toText() {
    return Integer.toString(this.code);
  }
}
