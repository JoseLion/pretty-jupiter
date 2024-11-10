package io.github.joselion.prettyjupiter.lib;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.testing.Test;

import lombok.Getter;

@Getter
public class PrettyJupiterExtension {

  @Nested
  private final Duration duration;

  @Nested
  private final Failure failure;

  @Inject
  public PrettyJupiterExtension(final ObjectFactory objects) {
    this.duration = objects.newInstance(Duration.class);
    this.failure = objects.newInstance(Failure.class);
  }

  public Duration getDuration() {
    return this.duration;
  }

  public Failure getFailure() {
    return this.failure;
  }

  public void duration(final Action<Duration> action) {
    action.execute(this.duration);
  }

  public void failure(final Action<Failure> action) {
    action.execute(this.failure);
  }

  @Getter
  public static class Duration {

    @Input
    private final Property<Boolean> enabled;

    @Input
    private final Property<Integer> threshold;

    @Input
    private final MapProperty<String, Integer> customThreshold;

    @Inject
    public Duration(final ObjectFactory objects) {
      this.enabled = objects.property(Boolean.class);
      this.threshold = objects.property(Integer.class);
      this.customThreshold = objects.mapProperty(String.class, Integer.class);

      this.enabled.convention(true);
      this.threshold.convention(200);
      this.customThreshold.convention(Map.of());
    }

    public Property<Boolean> getEnabled() {
      return this.enabled;
    }

    public Property<Integer> getThreshold() {
      return this.threshold;
    }

    public MapProperty<String, Integer> getCustomThreshold() {
      return this.customThreshold;
    }

    public Integer threshold(final Test testTask) {
      return Optional.of(testTask)
        .flatMap(this::findCustomThreshold)
        .orElseGet(this.threshold::get);
    }

    private Optional<Integer> findCustomThreshold(final Test testTask) {
      final var matcher = Pattern.compile("^task '.*:(\\w+)'$").matcher(testTask.toString());

      if (!matcher.matches()) {
        return Optional.empty();
      }

      return Optional.of(this.customThreshold)
        .map(MapProperty<String, Integer>::get)
        .map(thresholdMap -> thresholdMap.get(matcher.group(1)));
    }
  }

  @Getter
  public static class Failure {

    @Input
    private final Property<Integer> maxMessageLines;

    @Input
    private final Property<Integer> maxTraceLines;

    @Inject
    public Failure(final ObjectFactory objects) {
      this.maxMessageLines = objects.property(Integer.class);
      this.maxTraceLines = objects.property(Integer.class);

      this.maxMessageLines.convention(15);
      this.maxTraceLines.convention(15);
    }

    public Property<Integer> getMaxMessageLines() {
      return this.maxMessageLines;
    }

    public Property<Integer> getMaxTraceLines() {
      return this.maxTraceLines;
    }
  }
}
