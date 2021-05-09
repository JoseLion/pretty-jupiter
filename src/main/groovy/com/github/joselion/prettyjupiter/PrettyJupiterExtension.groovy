package com.github.joselion.prettyjupiter

import groovy.transform.CompileStatic
import org.gradle.api.Action

import javax.inject.Inject

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

@CompileStatic
class PrettyJupiterExtension {

  final Duration duration

  final Failure failure

  @Inject
  PrettyJupiterExtension(ObjectFactory objects) {
    this.duration = objects.newInstance(Duration)
    this.failure = objects.newInstance(Failure)
  }

  void duration(Action<Duration> durationAction) {
    durationAction.execute(duration)
  }

  void failure(Action<Failure> failureAction) {
    failureAction.execute(failure)
  }

  static class Duration {

    final Property<Boolean> enabled

    final Property<Integer> threshold

    @Inject
    Duration(ObjectFactory objects) {
      this.enabled = objects.property(Boolean)
      this.threshold = objects.property(Integer)

      this.enabled.convention(true)
      this.threshold.convention(75)
    }
  }

  static class Failure {

    final Property<Integer> maxMessageLines

    final Property<Integer> maxTraceLines

    @Inject
    Failure(ObjectFactory objects) {
      this.maxMessageLines = objects.property(Integer)
      this.maxTraceLines = objects.property(Integer)

      this.maxMessageLines.convention(15)
      this.maxTraceLines.convention(10)
    }
  }
}
