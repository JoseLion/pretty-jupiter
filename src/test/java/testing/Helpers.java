package testing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.api.tasks.testing.TestDescriptor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Helpers {

  @Nullable
  public static TestDescriptor descriptorOf(final Integer level) {
    if (level != null) {
      final var stub = mock(TestDescriptor.class);
      final var parent = level > 0 ? descriptorOf(level - 1) : null;
      final var name = level > 1 ? "Test description %d".formatted(level - 1) : "";

      when(stub.getParent()).thenReturn(parent);
      when(stub.getDisplayName()).thenReturn(name);

      return stub;
    }

    return null;
  }
}
