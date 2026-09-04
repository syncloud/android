package org.syncloud.android.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

const val DISCOVERY_UI_TIMEOUT_MS = 90_000L

@RunWith(AndroidJUnit4::class)
class DiscoveryScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<DevicesDiscoveryActivity>()

    @Test
    fun discoveryScreenListsADeviceFoundOverMdns() {
        compose.waitUntil(DISCOVERY_UI_TIMEOUT_MS) {
            compose.onAllNodesWithTag("discovered_item").fetchSemanticsNodes().isNotEmpty()
        }

        val nodes = compose.onAllNodesWithTag("discovered_item").fetchSemanticsNodes()
        assertTrue("discovery screen listed no device", nodes.isNotEmpty())

        capture("discovery-with-device")
    }

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(instrumentation.targetContext.filesDir, "screenshots")
        dir.mkdirs()
        FileOutputStream(File(dir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
