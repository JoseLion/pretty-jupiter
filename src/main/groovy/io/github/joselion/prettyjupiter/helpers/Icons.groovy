package io.github.joselion.prettyjupiter.helpers

enum Icons {
  SUCCESS(Utils.isTermDumb() ? '√' : Utils.coloredText(Colors.GREEN, '✔')),
  FAILURE(Utils.isTermDumb() ? 'X' : Utils.coloredText(Colors.RED, '✖')),
  SKIPPED(Utils.isTermDumb() ? '!' : '⚠️ ')

  private final String icon

  private Icons(String icon) {
    this.icon = icon
  }

  @Override
  String toString() {
    return icon
  }
}
