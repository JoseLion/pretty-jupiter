package testing;

import org.eclipse.jdt.annotation.Nullable;
import org.gradle.api.tasks.testing.TestDescriptor;

import lombok.Value;
import lombok.With;

@With
@Value
public class MockDescriptor implements TestDescriptor {

  private String name;

  private String displayName;

  private @Nullable String className;

  private boolean composite;

  private @Nullable TestDescriptor parent;

  public static MockDescriptor empty() {
    return new MockDescriptor("", "", null, false, null);
  }

  @Override
  public String getClassName() {
    return this.className();
  }

  @Override
  public String getDisplayName() {
    return this.displayName();
  }

  @Override
  public String getName() {
    return this.name();
  }

  @Override
  public TestDescriptor getParent() {
    return this.parent();
  }

  @Override
  public boolean isComposite() {
    return this.composite();
  }
}
