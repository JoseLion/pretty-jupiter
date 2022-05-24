package io.github.joselion.prettyjupiter.helpers

enum Icons {
  SUCCESS(Utils.coloredText(Colors.GREEN, '✔')),
  FAILURE(Utils.coloredText(Colors.RED, '✖')),
  SKIPPED('⚠️ ')

  private final String icon

  private Icons(String icon) {
    this.icon = icon
  }

  @Override
  String toString() {
    return icon
  }
}
