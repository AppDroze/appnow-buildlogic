package testutil

import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun runner(projectDir: File, vararg args: String) =
    GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath() // from pluginUnderTestMetadata
        .withArguments(*args)
        .forwardOutput()

