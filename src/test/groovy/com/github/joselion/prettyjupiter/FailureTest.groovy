package com.github.joselion.prettyjupiter

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC

import org.gradle.api.tasks.testing.TestDescriptor

import spock.lang.Specification

class FailureTest extends Specification {

  private static final Exception causeD = new Exception('Cause of error C')
  private static final Exception causeC = new Exception('Cause of error B', causeD)
  private static final Exception causeB = new Exception('Cause of error A', causeC)
  private static final AssertionError causeA = new AssertionError('Cause of top error', causeB)
  private static final Exception fullCause = new Exception('Top error', causeA)

  def '#cause'(Throwable topError, String result) {
    given:
      final Failure failure = new Failure(topError, desc(1), new PrettyJupiterPluginExtension())

    expect:
      failure.getCause() == result

    where:
      topError        | result
      new Exception() | null
      causeC          | "${ESC}[33m+ java.lang.Exception: Cause of error C${ESC}[0m"
      causeB          | "${ESC}[33m+ java.lang.Exception: Cause of error B\n" +
                                  "└─── java.lang.Exception: Cause of error C${ESC}[0m"
      causeA          | "${ESC}[33m+ java.lang.Exception: Cause of error A\n" +
                                  '└─┬─ java.lang.Exception: Cause of error B\n' +
                                  "  └─── java.lang.Exception: Cause of error C${ESC}[0m"
      fullCause       | "${ESC}[33m+ java.lang.AssertionError: Cause of top error\n" +
                                  '└─┬─ java.lang.Exception: Cause of error A\n' +
                                  '  └─┬─ java.lang.Exception: Cause of error B\n' +
                                  "    └─── java.lang.Exception: Cause of error C${ESC}[0m"
  }

  def '#location'(TestDescriptor descriptor, String location) {
    given:
      final Failure failure = new Failure(new Exception(), descriptor, new PrettyJupiterPluginExtension())

    expect:
      failure.getLocation() == location

    where:
      descriptor  | location
      desc()      | ''
      desc(1)     | 'Test description 1'
      desc(3)     | 'Test description 1 => Test description 2 => Test description 3'
  }

  def '#message'() {
    given:
      final String error = 'This should be an Assertion error!'
      final Failure failure = new Failure(new Exception(error), desc(1), new PrettyJupiterPluginExtension())

    expect:
      failure.getMessage() == "${ESC}[91mjava.lang.Exception: ${error}${ESC}[0m"
  }

  def '#trace'() {
    given:
      final String trace = '''java.lang.Exception: Some error message
  at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
  at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:64)
  at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
  at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
  at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
  at org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:80)
  at org.codehaus.groovy.runtime.callsite.ConstructorSite$ConstructorSiteNoUnwrapNoCoerce.callConstructor(ConstructorSite.java:105)
  at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallConstructor(CallSiteArray.java:59)
  at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:237)
  at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:249)
  --- and 60 more ---'''
      final Failure failure = new Failure(new Exception('Some error message'), desc(1), new PrettyJupiterPluginExtension())

    expect:
      failure.getTrace() == "${ESC}[90m${trace}${ESC}[0m"
  }

  private TestDescriptor desc(Integer parents = 0) {
    final Integer num = parents + 1

    return Stub(TestDescriptor) {
      getParent() >> Stub(TestDescriptor) {
        getParent() >> descriptorWithParents(num)
      }
    }
  }

  private TestDescriptor descriptorWithParents(Integer num) {
    if (num == null) {
      return null
    }

    if (num == 0) {
      return Stub(TestDescriptor) {
        getParent() >> null
        getDisplayName() >> "Test description ${num - 1}"
      }
    }

    return Stub(TestDescriptor) {
      getParent() >> descriptorWithParents(num - 1)
      getDisplayName() >> "Test description ${num - 1}"
    }
  }
}
