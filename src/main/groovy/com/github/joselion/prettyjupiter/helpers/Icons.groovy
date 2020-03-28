package com.github.joselion.prettyjupiter.helpers

enum Icons {
  SUCCESS(Utils.coloredText(Colors.GREEN, '✔')),
  FAILURE('❌'),
  SKIPPED('⚠️')

  private final String icon

  private Icons(String icon) {
    this.icon = icon
  }

  @Override
  public String toString() {
    return icon
  }
}
