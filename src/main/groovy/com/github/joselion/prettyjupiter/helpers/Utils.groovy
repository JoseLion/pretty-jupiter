package com.github.joselion.prettyjupiter.helpers

import org.gradle.api.tasks.testing.TestDescriptor

import com.github.joselion.prettyjupiter.helpers.Colors

public class Utils {

  public static final String ESC = Character.toString(27)

  public Utils() {
    throw new IllegalStateException("Utility class")
  }

  public static String getTabs(TestDescriptor descriptor, String tabChar = '  ') {
    final Integer level = getLevel(descriptor)

    return level > 0
      ? tabChar * level
      : ''
  }

  public static String coloredText(Colors color, String text) {
    final String colorCode = color.getCode()

    return "${ESC}[${colorCode}m${text}${ESC}[0m"
  }

  private static Integer getLevel(TestDescriptor descriptor, Integer acc = -2) {
    final TestDescriptor parent = descriptor?.getParent()

    return parent != null
      ? getLevel(parent, acc + 1)
      : acc
  }
}
