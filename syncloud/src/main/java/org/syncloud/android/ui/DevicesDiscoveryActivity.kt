package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.collect.Maps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.log4j.Logger
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.common.http.HttpClient
import org.syncloud.android.core.platform.Internal
import org.syncloud.android.core.platform.model.IdentifiedEndpoint
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter
import org.syncloud.android.ui.dialog.WifiDialog


class DevicesDiscoveryActivity : AppCompatActivity(),
        WifiDialog.NoticeDialogListener {
    private lateinit var preferences: Preferences
    private lateinit var discoveryManager: DiscoveryManager
    private lateinit var refreshBtn: FloatingActionButton
    private lateinit var listAdapter: DevicesDiscoveredAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var resultsList: ListView
    private lateinit var deviceToId: MutableMap<String, IdentifiedEndpoint>
    private lateinit var internal: Internal
    private lateinit var application: SyncloudApplication
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var wifiSettingsLauncher: ActivityResultLauncher<Intent>
    private val logger = Logger.getLogger(DevicesDiscoveryActivity::class.java.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices_discovery)
        application = getApplication() as SyncloudApplication
        preferences = application.preferences
        internal = Internal(WebService(HttpClient()))
        swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout.setOnRefreshListener { checkWiFiAndDiscover() }
        emptyView = findViewById(android.R.id.empty)
        resultsList = findViewById<View>(R.id.devices_discovered) as ListView
        refreshBtn = findViewById(R.id.discovery_refresh_btn)
        refreshBtn.setOnClickListener { checkWiFiAndDiscover() }
        listAdapter = DevicesDiscoveredAdapter(this)
        resultsList.adapter = listAdapter
        resultsList.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val obj = resultsList.getItemAtPosition(position)
            val ie = obj as IdentifiedEndpoint
            open(ie)
        }
        deviceToId = Maps.newHashMap()
        discoveryManager = DiscoveryManager(
                applicationContext.getSystemService(WIFI_SERVICE) as WifiManager,
                applicationContext.getSystemService(NSD_SERVICE) as NsdManager
        )
        swipeRefreshLayout.post { checkWiFiAndDiscover() }

        settingsLauncher = registerForActivityResult(StartActivityForResult()) {
            finish()
        }
        wifiSettingsLauncher = registerForActivityResult(StartActivityForResult()) {
            checkWiFiAndDiscover()
        }
    }

    private fun checkWiFiAndDiscover() {
        listAdapter.clear()
        if (application.isWifiConnected()) {
            refreshBtn.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = true
            emptyView.visibility = View.GONE
            resultsList.emptyView = null
            listAdapter.clear()

            CoroutineScope(Dispatchers.Main).launch {
                discover()
            }
        } else {
            val dialog = WifiDialog("Discovery is only possible on Wi-Fi.")
            dialog.show(supportFragmentManager, "discovery_wifi_dialog")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun open(endpoint: IdentifiedEndpoint) {
        val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://" + endpoint.device))
        startActivity(browserIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("leaving the screen")
        discoveryManager.cancel()
    }

    private suspend fun discover() {
        withContext(Dispatchers.IO) {
            discoveryManager.run(20) { e -> added(e) }
        }
        emptyView.visibility = View.VISIBLE
        resultsList.emptyView = emptyView
        swipeRefreshLayout.isRefreshing = false
        refreshBtn.visibility = View.VISIBLE

    }

    private suspend fun added(device: String) {
        val id = internal.getId(device)
        if (id != null) {
            val ie = IdentifiedEndpoint(device, id)
            withContext(Dispatchers.Main) {
                deviceToId[device] = ie
                listAdapter.add(ie)
            }
        }
    }

    override fun onDialogPositiveClick() {
        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    override fun onDialogNegativeClick() {
        finish()
    }
}