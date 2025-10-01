package doctor

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

class DoctorMinSdkTest {

    @Test
    fun `doctor fails when minSdk below policy floor`() {
        val dir = createTempDir(prefix = "doctor-minsdk-")
        write(dir, "settings.gradle.kts", """rootProject.name = "tmp" """)
        write(dir, "build.gradle.kts", """
            plugins { id("appnow.doctor") }
        """.trimIndent())
        write(dir, "gradle.properties", """
            android.minSdk=23
        """.trimIndent())

        val result = runner(dir, "appnowDoctor", "-Pappnow.minSupportedMinSdk=24").buildAndFail()
        assertTrue(result.output.contains("❌ android.minSdk=23 is below supported minimum (24)"))
    }
}
