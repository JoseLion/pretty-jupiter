package io.github.joselion.prettyjupiter.lib.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Icon {
  SUCCESS(Text.colored(Color.GREEN, "✔"), "√"),
  FAILURE(Text.colored(Color.RED, "✖"), "X"),
  SKIPPED(Text.colored(Color.YELLOW, "⚠️ "), "!");

  private final String text;

  private final String plain;

  @Override
  public String toString() {
    return Common.isTermDumb()
      ? this.plain
      : this.text;
  }
}
