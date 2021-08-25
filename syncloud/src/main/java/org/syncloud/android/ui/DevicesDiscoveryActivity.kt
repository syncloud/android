package org.syncloud.android.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.base.Optional
import com.google.common.collect.Maps
import org.apache.log4j.Logger
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.platform.Internal
import org.syncloud.android.core.platform.model.Endpoint
import org.syncloud.android.core.platform.model.IdentifiedEndpoint
import org.syncloud.android.discovery.DeviceEndpointListener
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.ui.DevicesDiscoveryActivity
import org.syncloud.android.ui.adapters.DevicesDiscoveredAdapter
import org.syncloud.android.ui.dialog.WifiDialog

class DevicesDiscoveryActivity : AppCompatActivity() {
    private var preferences: Preferences? = null
    private var discoveryManager: DiscoveryManager? = null
    private var refreshBtn: FloatingActionButton? = null
    private var listAdapter: DevicesDiscoveredAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var emptyView: View? = null
    private var resultsList: ListView? = null
    private var map: MutableMap<Endpoint?, IdentifiedEndpoint>? = null
    private var internal: Internal? = null
    private var application: SyncloudApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_devices_discovery)
        application = getApplication() as SyncloudApplication
        preferences = application!!.Preferences
        internal = Internal()
        swipeRefreshLayout = findViewById<View>(R.id.swipe_refresh_layout) as SwipeRefreshLayout
        swipeRefreshLayout!!.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout!!.setOnRefreshListener { checkWiFiAndDiscover() }
        emptyView = findViewById(android.R.id.empty)
        resultsList = findViewById<View>(R.id.devices_discovered) as ListView
        refreshBtn = findViewById<View>(R.id.discovery_refresh_btn) as FloatingActionButton
        listAdapter = DevicesDiscoveredAdapter(this)
        resultsList!!.adapter = listAdapter
        resultsList!!.onItemClickListener = OnItemClickListener { adapterView, view, position, l ->
            val obj = resultsList!!.getItemAtPosition(position)
            val ie = obj as IdentifiedEndpoint
            open(ie)
        }
        map = Maps.newHashMap()
        discoveryManager = DiscoveryManager(
            applicationContext.getSystemService(WIFI_SERVICE) as WifiManager,
            applicationContext.getSystemService(NSD_SERVICE) as NsdManager
        )
        swipeRefreshLayout!!.post { checkWiFiAndDiscover() }
    }

    private fun checkWiFiAndDiscover() {
        listAdapter!!.clear()
        if (application!!.isWifiConnected) {
            DiscoveryTask().execute()
        } else {
            val dialog = WifiDialog()
            dialog.setMessage("Discovery is possible only in the same Wi-Fi network where you have Syncloud device connected.")
            dialog.show(supportFragmentManager, "discovery_wifi_dialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WifiDialog.WIFI_SETTINGS) {
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
        if (!endpoint.id().isPresent) {
            AlertDialog.Builder(this)
                .setTitle("Can't identify device")
                .setMessage("Sorry, there's no identification information for this device. Most probably it is running old release of Syncloud. Please upgrade it to latest release and try to activate again.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(endpoint.endpoint().activationUrl()))
            startActivity(browserIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("leaving the screen")
        discoveryManager!!.cancel()
    }

    fun refresh(view: View?) {
        checkWiFiAndDiscover()
    }

    inner class DiscoveryTask : AsyncTask<Void, Progress, Void>() {
        private val deviceEndpointListener: DeviceEndpointListener
        override fun onPreExecute() {
            refreshBtn!!.visibility = View.GONE
            swipeRefreshLayout!!.isRefreshing = true
            emptyView!!.visibility = View.GONE
            resultsList!!.emptyView = null
            listAdapter!!.clear()

            //use for testing without wi-fi
            //listAdapter.add(new DirectEndpoint("localhost", 22, "vsapronov", "somepassword", null));
        }

        protected override fun doInBackground(vararg params: Void): Void? {
            discoveryManager!!.run(20, deviceEndpointListener)
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            emptyView!!.visibility = View.VISIBLE
            resultsList!!.emptyView = emptyView
            swipeRefreshLayout!!.isRefreshing = false
            refreshBtn!!.visibility = View.VISIBLE
        }

        protected override fun onProgressUpdate(vararg progresses: Progress) {
            val progress = progresses[0]
            val ie = progress.identifiedEndpoint
            if (progress.isAdded) {
                map!![progress.endpoint] = ie
                listAdapter!!.add(ie)
            } else {
                listAdapter!!.remove(ie)
            }
        }

        init {
            deviceEndpointListener = object : DeviceEndpointListener {
                override fun added(endpoint: Endpoint?) {
                    val id = internal!!.getId(
                        endpoint!!.host()
                    )
                    if (id.isPresent) {
                        val ie = IdentifiedEndpoint(endpoint, Optional.of(id.get()))
                        publishProgress(Progress(true, endpoint, ie))
                    }
                }

                override fun removed(endpoint: Endpoint?) {
                    val ie = map!!.remove(endpoint)
                    publishProgress(Progress(false, endpoint!!, ie!!))
                }
            }
        }
    }

    inner class Progress(
        isAdded: Boolean,
        endpoint: Endpoint,
        identifiedEndpoint: IdentifiedEndpoint
    ) {
        var isAdded = true
        var endpoint: Endpoint
        var identifiedEndpoint: IdentifiedEndpoint

        init {
            this.isAdded = isAdded
            this.endpoint = endpoint
            this.identifiedEndpoint = identifiedEndpoint
        }
    }

    companion object {
        private val logger = Logger.getLogger(
            DevicesDiscoveryActivity::class.java.name
        )
        private const val REQUEST_SETTINGS = 1
    }
}