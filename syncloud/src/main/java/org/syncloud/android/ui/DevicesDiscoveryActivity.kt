package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.collect.Maps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.apache.log4j.Logger
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.common.http.HttpClient
import org.syncloud.android.core.platform.Internal
import org.syncloud.android.core.platform.model.Endpoint
import org.syncloud.android.core.platform.model.IdentifiedEndpoint
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter
import org.syncloud.android.ui.dialog.WIFI_SETTINGS
import org.syncloud.android.ui.dialog.WifiDialog

const val REQUEST_SETTINGS = 1

class DevicesDiscoveryActivity : AppCompatActivity() {
    private lateinit var preferences: Preferences
    private lateinit var discoveryManager: DiscoveryManager
    private lateinit var refreshBtn: FloatingActionButton
    private lateinit var listAdapter: DevicesDiscoveredAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var resultsList: ListView
    private lateinit var map: MutableMap<Endpoint, IdentifiedEndpoint>
    private lateinit var internal: Internal
    private lateinit var application: SyncloudApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_devices_discovery)
        application = getApplication() as SyncloudApplication
        preferences = application.preferences
        internal = Internal(WebService(HttpClient()))
        swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout.setOnRefreshListener { checkWiFiAndDiscover() }
        emptyView = findViewById(android.R.id.empty)
        resultsList = findViewById<View>(R.id.devices_discovered) as ListView
        refreshBtn = findViewById<View>(R.id.discovery_refresh_btn) as FloatingActionButton
        listAdapter = DevicesDiscoveredAdapter(this)
        resultsList.adapter = listAdapter
        resultsList.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val obj = resultsList.getItemAtPosition(position)
            val ie = obj as IdentifiedEndpoint
            open(ie)
        }
        map = Maps.newHashMap()
        discoveryManager = DiscoveryManager(
                applicationContext.getSystemService(WIFI_SERVICE) as WifiManager,
                applicationContext.getSystemService(NSD_SERVICE) as NsdManager
        )
        swipeRefreshLayout.post { checkWiFiAndDiscover() }
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
            val dialog = WifiDialog("Discovery is possible only in the same Wi-Fi network where you have Syncloud device connected.")
            dialog.show(supportFragmentManager, "discovery_wifi_dialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WIFI_SETTINGS) {
            checkWiFiAndDiscover()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intentSettings = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intentSettings, REQUEST_SETTINGS)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun open(endpoint: IdentifiedEndpoint) {
        val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(endpoint.endpoint.activationUrl))
        startActivity(browserIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("leaving the screen")
        discoveryManager.cancel()
    }

    fun refresh(view: View?) {
        checkWiFiAndDiscover()
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

    private suspend fun added(endpoint: Endpoint) {
        val id = internal.getId(endpoint.host)
        if (id != null) {
            val ie = IdentifiedEndpoint(endpoint, id)
            withContext(Dispatchers.Main) {
                map[endpoint] = ie
                listAdapter.add(ie)
            }
        }
    }

    companion object {
        private val logger = Logger.getLogger(DevicesDiscoveryActivity::class.java.name)
    }
}