package com.github.joselion.prettyjupiter

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC

import org.gradle.api.tasks.testing.TestDescriptor
import spock.lang.Specification

class FailureTest extends Specification {

  def '#location'(TestDescriptor descriptor, String location) {
    given:
      final Failure failure = new Failure(new Exception(), descriptor, new PrettyJupiterPluginExtension())

    expect:
      failure.getLocation() == location

    where:
      descriptor  | location
      desc()      | ""
      desc(1)     | "Test description 1"
      desc(3)     | "Test description 1 => Test description 2 => Test description 3"
  }

  def "#message"() {
    given:
      final String error = "This should be an Assertion error!"
      final Failure failure = new Failure(new Exception(error), desc(1), new PrettyJupiterPluginExtension())

    expect:
      failure.getMessage() == "${ESC}[91mjava.lang.Exception: ${error}${ESC}[0m"
  }

  def "#trace"() {
    given:
      final String trace = '''java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:80)
org.codehaus.groovy.runtime.callsite.ConstructorSite$ConstructorSiteNoUnwrapNoCoerce.callConstructor(ConstructorSite.java:105)
org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallConstructor(CallSiteArray.java:59)
org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:237)
org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:241)
--- and 60 more ---'''
      final Failure failure = new Failure(new Exception(), desc(1), new PrettyJupiterPluginExtension())

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
