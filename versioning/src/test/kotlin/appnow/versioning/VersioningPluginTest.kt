package appnow.versioning

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.io.File

class VersioningPluginTest {

    private fun writeProps(dir: File, content: String) {
        dir.resolve("build-config.properties").writeText(content.trimIndent())
    }

    @Test
    fun `falls back to file when no -P provided`() {
        val project = ProjectBuilder.builder().build()
        writeProps(project.projectDir, """
            VERSION_NAME=9.9.9
            CATALOG_VERSION=9.9.9
            android.compileSdk=35
            android.minSdk=25
            android.targetSdk=35
            MIN_SUPPORTED_MIN_SDK=24
        """)

        project.pluginManager.apply("appnow.versioning")

        // extension values
        val ext = project.extensions.findByName("appnowVersioning") as AppnowVersioningExtension
        assertEquals("9.9.9", ext.versionName.get())
        assertEquals(35, ext.compileSdk.get())
        assertEquals(25, ext.minSdk.get())
        assertEquals(35, ext.targetSdk.get())

        // exported gradle properties
        assertEquals("9.9.9", project.findProperty("appnow.versionName"))
        assertEquals("35", project.findProperty("android.compileSdk"))
        assertEquals("25", project.findProperty("android.minSdk"))
    }

    @Test
    fun `-P properties override file values`() {
        val project = ProjectBuilder.builder().build()
        writeProps(project.projectDir, """
            VERSION_NAME=1.2.3
            CATALOG_VERSION=1.2.3
            android.compileSdk=34
            android.minSdk=24
            android.targetSdk=34
        """)

        // simulate -P
        project.extensions.extraProperties.set("VERSION_NAME", "2.0.0")
        project.extensions.extraProperties.set("CATALOG_VERSION", "2.0.0")
        project.extensions.extraProperties.set("android.compileSdk", "36")
        project.extensions.extraProperties.set("android.minSdk", "26")
        project.extensions.extraProperties.set("android.targetSdk", "36")

        project.pluginManager.apply("appnow.versioning")

        val ext = project.extensions.findByName("appnowVersioning") as AppnowVersioningExtension
        assertEquals("2.0.0", ext.versionName.get())
        assertEquals(36, ext.compileSdk.get())
        assertEquals(26, ext.minSdk.get())
        assertEquals(36, ext.targetSdk.get())

        assertEquals("2.0.0", project.findProperty("appnow.versionName"))
        assertEquals("36", project.findProperty("android.compileSdk"))
        assertEquals("26", project.findProperty("android.minSdk"))
        assertEquals("36", project.findProperty("android.targetSdk"))
    }

    @Test
    fun `fallbacks apply when nothing is provided`() {
        val project = ProjectBuilder.builder().build()
        // no file, no -P

        project.pluginManager.apply("appnow.versioning")

        val ext = project.extensions.findByName("appnowVersioning") as AppnowVersioningExtension
        assertNotNull(ext.versionName.get())          // default "0.0.1" per plugin
        assertEquals(36, ext.compileSdk.get())
        assertEquals(24, ext.minSdk.get())
        assertEquals(36, ext.targetSdk.get())

        // exported defaults present
        assertEquals("36", project.findProperty("android.compileSdk"))
        assertEquals("24", project.findProperty("android.minSdk"))
        assertEquals("36", project.findProperty("android.targetSdk"))
    }
}

