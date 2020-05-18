package com.github.joselion.prettyjupiter

import org.gradle.api.tasks.testing.TestDescriptor

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

class Failure {

  private String cause;

  private String location;

  private String message;

  private String trace;

  public Failure(Throwable exception, TestDescriptor descriptor, PrettyJupiterPluginExtension extension) {
    this.cause = buildCause(exception.getCause());
    this.location = buildLocation(descriptor, Utils.getLevel(descriptor))
    this.message = buildMessage(exception.toString(), extension.failure.maxMessageLines)
    this.trace = buildTrace(exception, extension.failure.maxTraceLines)
  }

  public String getCause() {
    return cause
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
      final String concatText = text.isEmpty()
        ? desc.getDisplayName()
        : "${desc.getDisplayName()} => ${text}"

      return buildLocation(desc.getParent(), i - 1, concatText)
    }

    return text
  }

  private String buildMessage(String message, Integer maxLines) {
    final String limitedMessage = Utils.limitedText(message, maxLines)

    return Utils.coloredText(Colors.BRIGHT_RED, limitedMessage)
  }

  private String buildTrace(Throwable exception, Integer maxLines) {
    final String firstLine = exception.toString().replace('\n', ' ') + '\n'
    final String rest = exception.getStackTrace().collect { "at ${it}" }.join('\n')
    final String limitedMessage = Utils.limitedText(rest, maxLines)
    final String traceText = limitedMessage.split('\n').collect{ "  ${it}" }.join('\n')

    return Utils.coloredText(Colors.GRAY, firstLine + traceText)
  }

  private String buildCause(Exception cause) {
    if (cause) {
      final String causeText = "+ ${cause.toString()}" + getNextCause(cause.getCause())
      return Utils.coloredText(Colors.YELLOW, causeText)
    }

    return null;
  }

  private String getNextCause(Throwable cause, int indent = 0) {
    if (cause) {
      final String ns = ' ' * indent;
      final String symbol = cause.getCause() ? '┬' : '─'
      final String next = getNextCause(cause.getCause(), indent + 2)
      return "\n${ns}└─${symbol}─ ${cause.toString()}" + next
    }

    return '';
  }
}
