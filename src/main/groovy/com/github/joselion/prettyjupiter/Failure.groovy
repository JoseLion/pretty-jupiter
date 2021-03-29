package com.github.joselion.prettyjupiter

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

import org.gradle.api.tasks.testing.TestDescriptor

class Failure {

  private final String cause

  private final String location

  private final String message

  private final String trace

  Failure(Throwable exception, TestDescriptor descriptor, PrettyJupiterPluginExtension extension) {
    this.cause = buildCause(exception.getCause())
    this.location = buildLocation(descriptor, Utils.getLevel(descriptor))
    this.message = buildMessage(exception.toString(), extension.failure.maxMessageLines)
    this.trace = buildTrace(exception, extension.failure.maxTraceLines)
  }

  String getCause() {
    return cause
  }

  String getLocation() {
    return location
  }

  String getMessage() {
    return message
  }

  String getTrace() {
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

  private String buildCause(Throwable cause) {
    if (cause) {
      final String causeText = "+ ${cause}" + getNextCause(cause.getCause())
      return Utils.coloredText(Colors.YELLOW, causeText)
    }

    return null
  }

  private String getNextCause(Throwable cause, int indent = 0) {
    if (cause) {
      final String ns = ' ' * indent
      final String symbol = cause.getCause() ? '┬' : '─'
      final String next = getNextCause(cause.getCause(), indent + 2)
      return "\n${ns}└─${symbol}─ ${cause}" + next
    }

    return ''
  }
}
