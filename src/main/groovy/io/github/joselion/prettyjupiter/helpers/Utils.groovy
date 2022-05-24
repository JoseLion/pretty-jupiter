package io.github.joselion.prettyjupiter.helpers

import java.util.stream.Collectors

import org.gradle.api.tasks.testing.TestDescriptor

class Utils {

  static final String ESC = Character.toString(27)

  Utils() {
    throw new IllegalStateException('Utility class')
  }

  static String getTabs(TestDescriptor descriptor, String tabChar = '  ') {
    final Integer level = getLevel(descriptor)

    return level > 0
      ? tabChar * level
      : ''
  }

  static String coloredText(Colors color, String text) {
    final String colorCode = color.getCode()

    return "${ESC}[${colorCode}m${text}${ESC}[0m"
  }

  static String limitedText(String text, Integer maxLines) {
    if (text == null) {
      return null
    }

    final long lines = text.lines().count()

    if (maxLines == 0) {
      return "--- not showing ${lines} lines of text ---"
    }

    if (lines > maxLines) {
      final String limited = text.lines().limit(maxLines).collect(Collectors.joining('\n'))
      return "${limited}\n--- and ${lines - maxLines} more ---"
    }

    return text
  }

  static Integer getLevel(TestDescriptor descriptor, Integer acc = -2) {
    final TestDescriptor parent = descriptor?.getParent()

    return parent != null
      ? getLevel(parent, acc + 1)
      : acc
  }

  static String uncolorText(String text) {
    return text.replace("$ESC", '')
      .replaceAll(/\[\d*m/, '')
  }
}
