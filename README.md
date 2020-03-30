# Pretty Jupiter Plugin

## Why?
JUnit 5 brings to us the [@Nested](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested) annotation feature, which allows the test writer to group tests with similar conditions (initializations, relationships, etc.), and lets us add some BDD love to our test suites. This works great until we run the test suite with Gradle's `test` task. Even if we change the [testLogging](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.logging.TestLoggingContainer.html) closure on our build script to log more information and events about the tests, we'll end up with some not-very-friendly to read logs.

This plugin intends to solve that by grouping test logs and presenting them in a more readable, understandable and prettier way. Moving failure traces to the end so they could be tackled more easily and finishing with a pretty summary. Durations, their color threshold, and failure traces can be configured using the `prettyJupiter` extension.

## Usage
Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):
```groovy
plugins {
  id 'com.github.joselion.pretty-jupiter' version '1.0.0'
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):
```groovy
buildscript {
  repositories {
    maven {
      url 'https://plugins.gradle.org/m2/'
    }
  }
  dependencies {
    classpath 'gradle.plugin.com.github.joselion.pretty-jupiter:pretty-jupiter:1.0.0'
  }
}

apply plugin: 'com.github.joselion.pretty-jupiter'
```

## Extension properties
The plugin can be customized adding a `prettyJupiter` closure to your `build.gradle` file and changing the following properties:

| Property                | Default   | Description |
| ----------------------- |:---------:| ----------- |
| duration                | -         | Closure to configure the test durations logged |
| duration.enabled        | `true`    | If `true`, shows each test execution duration |
| duration.threshold      | `75`      | Time threshold in milliseconds. If the test duration is `>=` than this value, it'll be colored <span style="color:red">RED</span>, if it's `>=` than half of this value, it'll be <span style="color:yellow">YELLOW</span>, otherwise it'll be white. |
| failure                 | -         | Closure to configure the test failures logged |
| failure.maxMessageLines | `15`      | The number of lines of the exception message to display. Note that some exception messages may include some stack trace on it |
| failure.maxTraceLines   | `10`      | The number of lines of the exception stack trace to display |

### Complete example

```groovy
prettyJupiter {
  duration {
    enabled = true
    threshold = 75
  }

  failure {
    maxMessageLines = 15
    maxTraceLines = 10
  }
}
```

## Illustrations

### Before
Adding the following to `build.gradle` file:

```groovy
test {
  useJUnitPlatform()

  testLogging {
    exceptionFormat 'short'
    events 'started', 'skipped', 'failed'
  }
}
```

![Before](assets/before.png)

### With `pretty-jupiter` plugin applied
We only need Junit 5 configuration in `build.gradle` file:
```groovy
test {
  useJUnitPlatform()
}
```

![After (tests duration)](assets/after-durations.png)
![After (tests result)](assets/after-result.png)

## Want to add further customizations?
Please create an [issue](https://github.com/JoseLion/pretty-jupiter/issues/new) describing your request, feature or bug. I'll try to look into it as soon as possible 🙂

## Contribution
Contributions are very welcome! To do so, please fork this repository and open a Pull Request to the `master` branch.

## License

[Apache License 2.0](LICENSE)
