package io.github.joselion.prettyjupiter.lib;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.api.tasks.testing.TestDescriptor;

import io.github.joselion.prettyjupiter.lib.helpers.Color;
import io.github.joselion.prettyjupiter.lib.helpers.Common;
import io.github.joselion.prettyjupiter.lib.helpers.Text;

public record Failure(
  @Nullable String cause,
  String location,
  String message,
  String trace
) {

  public static Failure of(
    final Throwable throwable,
    final TestDescriptor descriptor,
    final PrettyJupiterExtension extension
  ) {
    return new Failure(
      causeOf(throwable),
      locationOf(descriptor),
      messageOf(throwable, extension),
      traceOf(throwable, extension)
    );
  }

  @Nullable
  private static String causeOf(final Throwable exception) {
    final var cause = exception.getCause();

    if (cause != null) {
      final var next = nextCause(cause.getCause());
      final var causeText = "+ ".concat(cause.toString()).concat(next);

      return Text.colored(Color.YELLOW, causeText);
    }

    return null;
  }

  private static String locationOf(final @Nullable TestDescriptor desc) {
    return locationOf(desc, Common.levelFor(desc), "");
  }

  private static String locationOf(final @Nullable TestDescriptor desc, final int i, final String text) {
    if (desc != null && i >= 0) {
      final var concatText = text.isEmpty()
        ? desc.getDisplayName()
        : desc.getDisplayName().concat("\n").concat("  ".repeat(i + 1)).concat(text);

      return locationOf(desc.getParent(), i - 1, concatText);
    }

    return text;
  }

  private static String messageOf(final Throwable exception, final PrettyJupiterExtension extension) {
    final var maxLines = extension.failure().maxMessageLines().get();
    final var limitedMessage = Text.limited(exception.toString(), maxLines);

    return Text.colored(Color.BRIGHT_RED, limitedMessage);
  }

  private static String traceOf(final Throwable exception, final PrettyJupiterExtension extension) {
    final var maxLines = extension.failure().maxTraceLines().get();
    final var firstLine = exception.toString().replace("\n", " ").concat("\n");
    final var rest = stream(exception.getStackTrace())
        .map(StackTraceElement::toString)
        .map("at "::concat)
        .collect(joining("\n"));
    final var limitedMessage = Text.limited(rest, maxLines);
    final var traceText = stream(limitedMessage.split("\n"))
        .map("  "::concat)
        .collect(joining("\n"));

    return Text.colored(Color.GRAY, firstLine.concat(traceText));
  }

  private static String nextCause(@Nullable final Throwable cause) {
    return nextCause(cause, 0);
  }

  private static String nextCause(@Nullable final Throwable cause, final int indent) {
    if (cause != null) {
      final var ns = " ".repeat(indent);
      final var symbol = cause.getCause() != null ? "┬" : "─";
      final var next = nextCause(cause.getCause(), indent + 2);

      return "\n".concat(ns).concat("└─").concat(symbol).concat("─ ").concat(cause.toString()).concat(next);
    }

    return "";
  }
}
