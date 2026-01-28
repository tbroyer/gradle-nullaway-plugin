package net.ltgt.gradle.nullaway;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.Nullable;

public class NullAwayPlugin implements Plugin<Project> {
  static final String PLUGIN_ID = "net.ltgt.nullaway";
  static final String EXTENSION_NAME = "nullaway";

  @Override
  public void apply(Project project) {
    if (GradleVersion.current().compareTo(GradleVersion.version("6.8")) < 0) {
      throw new UnsupportedOperationException(PLUGIN_ID + " requires at least Gradle 6.8");
    }

    NullAwayExtension extension =
        project.getExtensions().create(EXTENSION_NAME, NullAwayExtension.class);

    project
        .getPluginManager()
        .withPlugin(
            ErrorPronePlugin.PLUGIN_ID,
            ignored -> {
              project
                  .getTasks()
                  .withType(JavaCompile.class)
                  .configureEach(
                      task -> {
                        ErrorProneOptions errorproneOptions =
                            ((ExtensionAware) task.getOptions())
                                .getExtensions()
                                .getByType(ErrorProneOptions.class);
                        NullAwayOptions nullawayOptions =
                            ((ExtensionAware) errorproneOptions)
                                .getExtensions()
                                .create(EXTENSION_NAME, NullAwayOptions.class, extension);

                        errorproneOptions
                            .getErrorproneArgumentProviders()
                            .add(new NullAwayArgumentProvider(nullawayOptions));
                      });
            });
  }

  private static class NullAwayArgumentProvider implements CommandLineArgumentProvider, Named {
    private final NullAwayOptions nullawayOptions;

    NullAwayArgumentProvider(NullAwayOptions nullawayOptions) {
      this.nullawayOptions = nullawayOptions;
    }

    @Internal
    @Override
    public String getName() {
      return EXTENSION_NAME;
    }

    @SuppressWarnings("unused")
    @Nested
    @Optional
    @Nullable NullAwayOptions getNullAwayOptions() {
      return nullawayOptions.getSeverity().getOrElse(CheckSeverity.DEFAULT) == CheckSeverity.OFF
          ? null
          : nullawayOptions;
    }

    @Override
    public Iterable<String> asArguments() {
      return nullawayOptions.asArguments();
    }
  }
}
