package com.github.joselion.prettyjupiter.helpers

enum Colors {
  GRAY(90),
  GREEN(32),
  RED(31),
  YELLOW(33),
  WHITE(97)

  private final int code

  private Colors(int code) {
    this.code = code
  }

  public int getCode() {
    code
  }
}
