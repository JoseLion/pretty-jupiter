package testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;

import org.mockito.Mockito;

public interface Spy {

  @SuppressWarnings("unchecked")
  static <T> T lambda(final T lambda) {
    final var interfaces = lambda.getClass().getInterfaces();
    assertThat(interfaces).hasSize(1);

    return Mockito.mock((Class<T>) interfaces[0], delegatesTo(lambda));
  }
}
