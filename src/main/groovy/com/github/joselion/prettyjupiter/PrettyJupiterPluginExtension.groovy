package com.github.joselion.prettyjupiter

class PrettyJupiterPluginExtension {

  Duration duration = new Duration()

  Failure failure = new Failure()

  static class Duration {

    Boolean enabled = true

    Long threshold = 75
  }

  static class Failure {

    Integer maxMessageLines = 15

    Integer maxTraceLines = 10
  }
}
