package io.github.joselion.prettyjupiter.lib.helpers;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = PRIVATE)
public enum Icon {
  SUCCESS("✓", Color.GREEN),
  FAILURE("𐄂", Color.RED),
  SKIPPED("↷", Color.YELLOW);

  private final String text;

  private final Color color;

  @Override
  public String toString() {
    return Text.colored(this.color, this.text);
  }
}
