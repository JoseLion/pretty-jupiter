package io.github.joselion.prettyjupiter.lib.helpers;

import static java.util.stream.Collectors.joining;

public final class Text {

  private static final String ESC = Character.toString(27);

  private Text() {
    throw new UnsupportedOperationException("Text is a helper class");
  }

  public static String colored(final Color color, final String text) {
    return !Common.isTermDumb()
      ? ESC.concat("[").concat(color.toText()).concat("m").concat(text).concat(ESC).concat("[0m")
      : text;
  }

  public static String limited(final String text, final int maxLines) {
    final var lines = text.lines().count();

    if (maxLines == 0) {
      return "--- not showing ".concat(Long.toString(lines)).concat(" lines of text ---");
    }

    if (lines > maxLines) {
      final var limited = text.lines().limit(maxLines).collect(joining("\n"));
      final var diff = lines - maxLines;

      return limited.concat("\n--- and ").concat(Long.toString(diff)).concat(" more ---");
    }

    return text;
  }

  public static String uncolored(final String text) {
    return text
      .replace(ESC, "")
      .replaceAll("\\[\\d*m", "");
  }
}
