package io.github.joselion.prettyjupiter.lib.helpers;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.api.tasks.testing.TestDescriptor;

import groovy.lang.Closure;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Common {

  public static <T> Closure<Void> closure(final Consumer<T> consumer) {
    return new Closure<Void>(null) {

      @Nullable
      public Void doCall(final T arg) {
        consumer.accept(arg);
        return null;
      }
    };
  }

  public static <T, S> Closure<Void> closure(final BiConsumer<T, S> consumer) {
    return new Closure<Void>(null) {

      @Nullable
      public Void doCall(final T arg1, final S arg2) {
        consumer.accept(arg1, arg2);
        return null;
      }
    };
  }

  public static int levelFor(final @Nullable TestDescriptor descriptor) {
    return levelFor(descriptor, -2);
  }

  private static int levelFor(final @Nullable TestDescriptor descriptor, final int acc) {
    return Optional.ofNullable(descriptor)
      .map(TestDescriptor::getParent)
      .map(d -> levelFor(d, acc + 1))
      .orElse(acc);
  }

  public static String tabsFor(final @Nullable TestDescriptor descriptor) {
    final var level = levelFor(descriptor, -2);

    return level > 0
      ? "  ".repeat(level)
      : "";
  }

  public static boolean isTermDumb() {
    return Optional.of("TERM")
      .map(System::getenv)
      .map("dumb"::equalsIgnoreCase)
      .orElse(false);
  }
}
