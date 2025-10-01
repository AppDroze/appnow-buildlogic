package appnow.tests

import kotlin.test.Test
import kotlin.test.assertTrue
import testutil.runner
import kotlin.io.path.createTempDirectory

class DoctorAndroidXTest {
    @Test
    fun doctorFailsWhenAndroidXOff() {
        val dir = createTempDirectory(prefix = "doctor-").toFile()
        dir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "test"
            """.trimIndent()
        )
        dir.resolve("gradle.properties").writeText(
            """
            android.useAndroidX=false
            android.compileSdk=36
            android.minSdk=24
            android.targetSdk=36
            """.trimIndent()
        )

        dir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("appnow.doctor") }
            """.trimIndent()
        )

        val result = runner(dir, "appnowDoctor").buildAndFail()
        assertTrue(result.output.contains("android.useAndroidX must be true"))
        dir.deleteRecursively()
    }
}

