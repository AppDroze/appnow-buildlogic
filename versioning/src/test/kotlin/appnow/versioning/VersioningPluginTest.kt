package appnow.versioning

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

private fun runner(dir: File, vararg args: String) =
    GradleRunner.create()
        .withProjectDir(dir)
        .withPluginClasspath()
        .withArguments(*args, "--stacktrace")
        .forwardOutput()

private fun write(dir: File, path: String, content: String) {
    val f = File(dir, path); f.parentFile.mkdirs(); f.writeText(content)
}

class VersioningPluginTest {

    @Test
    fun `sets project version from -P VERSION_NAME when unspecified`() {
        val dir = createTempDir(prefix = "ver-plugin-")
        write(dir, "settings.gradle.kts", """rootProject.name = "tmp" """)
        write(dir, "build.gradle.kts", """
            plugins { id("appnow.versioning") }
            tasks.register("echo") { doLast { println("ver=" + project.version) } }
        """.trimIndent())

        val res = runner(dir, "echo", "-PVERSION_NAME=1.2.3").build()
        assertTrue(res.output.contains("ver=1.2.3"))
    }

    @Test
    fun `does not override explicit version`() {
        val dir = createTempDir(prefix = "ver-plugin-")
        write(dir, "settings.gradle.kts", """rootProject.name = "tmp" """)
        write(dir, "build.gradle.kts", """
            plugins { id("appnow.versioning") }
            version = "9.9.9"
            tasks.register("echo") { doLast { println("ver=" + project.version) } }
        """.trimIndent())

        val res = runner(dir, "echo", "-PVERSION_NAME=1.2.3").build()
        assertTrue(res.output.contains("ver=9.9.9"))
    }
}