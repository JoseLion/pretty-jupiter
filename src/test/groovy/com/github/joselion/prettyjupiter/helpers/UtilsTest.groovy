package com.github.joselion.prettyjupiter.helpers

import static com.github.joselion.prettyjupiter.helpers.Utils.ESC

import spock.lang.Specification
import org.gradle.api.tasks.testing.TestDescriptor

import com.github.joselion.prettyjupiter.helpers.Colors
import com.github.joselion.prettyjupiter.helpers.Utils

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
      color         | text
      Colors.GRAY   | "${ESC}[90mThis is a colored text!${ESC}[0m"
      Colors.GREEN  | "${ESC}[32mThis is a colored text!${ESC}[0m"
      Colors.RED    | "${ESC}[31mThis is a colored text!${ESC}[0m"
      Colors.YELLOW | "${ESC}[33mThis is a colored text!${ESC}[0m"
      Colors.WHITE  | "${ESC}[97mThis is a colored text!${ESC}[0m"
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
