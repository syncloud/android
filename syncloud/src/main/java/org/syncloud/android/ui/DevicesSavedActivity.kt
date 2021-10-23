package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.platform.model.DomainModel
import org.syncloud.android.core.redirect.model.toModels
import org.syncloud.android.ui.adapters.DevicesSavedAdapter

class DevicesSavedActivity : AppCompatActivity() {
    private lateinit var listview: ListView
    private lateinit var adapter: DevicesSavedAdapter
    private lateinit var application: SyncloudApplication
    private lateinit var preferences: Preferences
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var btnDiscovery: FloatingActionButton
    private lateinit var emptyView: View
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices_saved)
        emptyView = findViewById(android.R.id.empty)
        listview = findViewById(R.id.devices_saved)
        listview.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val obj = listview.getItemAtPosition(position)
            val domain = obj as DomainModel
            open(domain)
        }
        btnDiscovery = findViewById(R.id.discovery_btn)
        btnDiscovery.setOnClickListener {
            activityLauncher.launch(Intent(this, DevicesDiscoveryActivity::class.java))
        }
        adapter = DevicesSavedAdapter(this)
        listview.adapter = adapter
        application = getApplication() as SyncloudApplication
        preferences = application.preferences
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout.setOnRefreshListener { refreshDevices() }
        swipeRefreshLayout.post { refreshDevices() }

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshDevices()
        }
    }

    private fun refreshDevices() {
        val userService = application.userServiceCached
        val redirectEmail = preferences.redirectEmail
        val redirectPassword = preferences.redirectPassword
        if (redirectEmail != null && redirectPassword != null) {
            emptyView.visibility = View.GONE
            listview.emptyView = null
            adapter.clear()

            swipeRefreshLayout.isRefreshing = true
            listview.isEnabled = false
            btnDiscovery.visibility = View.GONE

            CoroutineScope(Dispatchers.IO).launch {
                val user = userService.getUser(redirectEmail, redirectPassword)
                val domains = user?.domains?.toModels() ?: listOf()
                val sortedDomains = domains.sortedWith{ first, second ->
                    first.name.compareTo(second.name)
                }
                withContext(Dispatchers.Main) {
                    adapter.clear()
                    adapter.addAll(sortedDomains)
                    swipeRefreshLayout.isRefreshing = false
                    listview.isEnabled = true
                    btnDiscovery.visibility = View.VISIBLE
                    emptyView.visibility = View.VISIBLE
                    listview.emptyView = emptyView
                }
            }
        }
    }

    private fun open(device: DomainModel) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(device.dnsUrl())))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            activityLauncher.launch(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}