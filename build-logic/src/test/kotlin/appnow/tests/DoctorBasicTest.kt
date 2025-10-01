package appnow.tests

import kotlin.test.Test
import kotlin.test.assertTrue
import testutil.runner
import kotlin.io.path.createTempDirectory

class DoctorBasicTest {
    
    @Test
    fun doctorPassesWithValidConfig() {
        val dir = createTempDirectory(prefix = "doctor-").toFile()
        dir.resolve("settings.gradle.kts").writeText(
            """
            rootProject.name = "test"
            """.trimIndent()
        )
        dir.resolve("gradle.properties").writeText(
            """
            android.useAndroidX=true
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

        val result = runner(dir, "appnowDoctor").build()
        assertTrue(result.output.contains("✅ Doctor check complete"))
        assertTrue(result.output.contains("AndroidX enabled"))
        dir.deleteRecursively()
    }
}

