package com.github.joselion.prettyjupiter

public class PrettyJupiterPluginExtension {

  Duration duration = new Duration()

  Failure failure = new Failure()

  public static class Duration {

    Boolean enabled = true

    Long threshold = 75
  }

  public static class Failure {

    Integer maxMessageLines = 15

    Integer maxTraceLines = 10
  }
}
