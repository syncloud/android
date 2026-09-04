package org.syncloud.android.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections

const val DISCOVERY_SECONDS = 30

@RunWith(AndroidJUnit4::class)
class DiscoveryInstrumentedTest {

    @Test
    fun discoversSyncloudDeviceOnTheNetwork() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = DiscoveryManager(
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager,
            context.getSystemService(Context.NSD_SERVICE) as NsdManager
        )

        val devices = Collections.synchronizedList(mutableListOf<String>())
        manager.run(DISCOVERY_SECONDS) { device -> devices.add(device) }

        assertTrue(
            "no device discovered over mdns in $DISCOVERY_SECONDS seconds",
            devices.isNotEmpty()
        )
    }
}
