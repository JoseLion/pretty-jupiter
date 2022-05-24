package io.github.joselion.prettyjupiter.helpers

import static io.github.joselion.prettyjupiter.helpers.Utils.ESC

import org.gradle.api.tasks.testing.TestDescriptor

import spock.lang.Specification

class UtilsTest extends Specification {

  def 'contructor'() {
    when:
      new Utils()

    then:
      IllegalStateException exception = thrown()
      exception.message == 'Utility class'
  }

  def '.getTabs'(Integer parents, String tabs) {
    expect:
      Utils.getTabs(descriptorWithParents(parents)) == tabs

    where:
      parents | tabs
      null    | ''
      0       | ''
      1       | ''
      2       | ''
      3       | '  '
      4       | '    '
      5       | '      '
  }

  def '.coloredText'(Colors color, String text) {
    expect:
      Utils.coloredText(color, 'This is a colored text!') == text

    where:
      color             | text
      Colors.BRIGHT_RED | "${ESC}[91mThis is a colored text!${ESC}[0m"
      Colors.GRAY       | "${ESC}[90mThis is a colored text!${ESC}[0m"
      Colors.GREEN      | "${ESC}[32mThis is a colored text!${ESC}[0m"
      Colors.RED        | "${ESC}[31mThis is a colored text!${ESC}[0m"
      Colors.YELLOW     | "${ESC}[33mThis is a colored text!${ESC}[0m"
      Colors.WHITE      | "${ESC}[97mThis is a colored text!${ESC}[0m"
  }

  def '.limitedText'(Integer limit, String result) {
    expect:
      Utils.limitedText('Multiple lines text\n' * 5, limit) == result
      Utils.limitedText(null, 1) == null

    where:
      limit | result
      0     | '--- not showing 5 lines of text ---'
      1     | 'Multiple lines text\n--- and 4 more ---'
      3     | 'Multiple lines text\nMultiple lines text\nMultiple lines text\n--- and 2 more ---'
      7     | 'Multiple lines text\nMultiple lines text\nMultiple lines text\nMultiple lines text\nMultiple lines text\n'
  }

  def '.uncolorText'() {
    given:
      final String greeting = 'Hello World!'
      final String colored = Utils.coloredText(Colors.RED, greeting)

    when:
      final String uncolored = Utils.uncolorText(colored)

    then:
      uncolored == greeting
  }

  private TestDescriptor descriptorWithParents(Integer num) {
    if (num == null) {
      return null
    }

    if (num == 0) {
      return Stub(TestDescriptor) {
        getParent() >> null
      }
    }

    return Stub(TestDescriptor) {
      getParent() >> descriptorWithParents(num - 1)
    }
  }
}
