package com.github.joselion.prettyjupiter

import org.gradle.api.tasks.testing.TestDescriptor

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

class Failure {

  private String location;

  private String message;

  private String trace;

  public Failure(Throwable exception, TestDescriptor descriptor, PrettyJupiterPluginExtension extension) {
    this.location = buildLocation(descriptor, Utils.getLevel(descriptor))
    this.message = buildMessage(exception.getMessage(), extension.failure.maxMessageLines)
    this.trace = buildTrace(exception.getStackTrace().join('\n'), extension.failure.maxTraceLines)
  }

  public String getLocation() {
    return location
  }

  public String getMessage() {
    return message
  }

  public String getTrace() {
    return trace
  }

  private String buildLocation(TestDescriptor desc, Integer i, String text = '') {
    if (i >= 0) {
      final String cocatText = text.isEmpty()
        ? desc.getDisplayName()
        : "${desc.getDisplayName()} => ${text}"

      return buildLocation(desc.getParent(), i - 1, cocatText)
    }

    return text
  }

  private String buildMessage(String message, Integer maxLines) {
    final String limitedMessage = Utils.limitedText(message, maxLines)

    return Utils.coloredText(Colors.BRIGHT_RED, limitedMessage)
  }

  private String buildTrace(String trace, Integer maxLines) {
    final String limitedMessage = Utils.limitedText(trace, maxLines)

    return Utils.coloredText(Colors.GRAY, limitedMessage)
  }
}
