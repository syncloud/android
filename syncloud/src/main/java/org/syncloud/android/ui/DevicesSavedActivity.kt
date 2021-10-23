package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.syncloud.android.Preferences
import org.syncloud.android.Progress
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.platform.model.DomainModel
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.core.redirect.model.toModels
import org.syncloud.android.tasks.AsyncResult
import org.syncloud.android.tasks.ProgressAsyncTask
import org.syncloud.android.ui.adapters.DevicesSavedAdapter

class DevicesSavedActivity : AppCompatActivity() {
    private lateinit var listview: ListView
    private lateinit var adapter: DevicesSavedAdapter
    private lateinit var application: SyncloudApplication
    private lateinit var preferences: Preferences
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var btnDiscovery: FloatingActionButton
    private lateinit var emptyView: View
    private val progress: Progress = ProgressImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_devices_saved)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        emptyView = findViewById(android.R.id.empty)
        listview = findViewById(R.id.devices_saved)
        listview.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, l: Long ->
            val obj = listview.getItemAtPosition(position)
            val domain = obj as DomainModel
            open(domain)
        }
        btnDiscovery = findViewById(R.id.discovery_btn)
        adapter = DevicesSavedAdapter(this)
        listview.adapter = adapter
        application = getApplication() as SyncloudApplication
        preferences = application.preferences
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout.setOnRefreshListener { refreshDevices() }
        swipeRefreshLayout.post { refreshDevices() }
    }

    private fun refreshDevices() {
        val userService = application.userServiceCached
        val redirectEmail = preferences.redirectEmail
        val redirectPassword = preferences.redirectPassword
        if (redirectEmail != null && redirectPassword != null) {
            emptyView.visibility = View.GONE
            listview.emptyView = null
            adapter.clear()
            ProgressAsyncTask<Void, User>()
                    .setProgress(progress)
                    .doWork(object : ProgressAsyncTask.Work<Void, User> {
                        override fun run(vararg args: Void): User {
                            return userService.getUser(redirectEmail, redirectPassword)!!
                        }
                    })
                    .onCompleted(object : ProgressAsyncTask.Completed<User> {
                        override fun run(result: AsyncResult<User>?) {
                            emptyView.visibility = View.VISIBLE
                            listview.emptyView = emptyView
                        }
                    })
                    .onSuccess(object : ProgressAsyncTask.Success<User> {
                        override fun run(result: User?) {
                            updateUser(result)
                        }
                    })
                    .execute()

        }
    }

    private fun updateUser(user: User?) {
        val domains = user?.domains?.toModels() ?: listOf()
        val sortedDomains = domains.sortedWith{ first: DomainModel, second: DomainModel ->
            first.name.compareTo(second.name)
        }
        adapter.clear()
        adapter.addAll(sortedDomains)
    }

    private fun open(device: DomainModel) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(device.dnsUrl())))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshDevices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivityForResult(Intent(this, SettingsActivity::class.java), 2)
        }
        return super.onOptionsItemSelected(item)
    }

    fun discover(view: View?) {
        startActivityForResult(Intent(this, DevicesDiscoveryActivity::class.java), 1)
    }

    inner class ProgressImpl : Progress.Empty() {
        override fun start() {
            swipeRefreshLayout.isRefreshing = true
            listview.isEnabled = false
            btnDiscovery.visibility = View.GONE
        }

        override fun stop() {
            swipeRefreshLayout.isRefreshing = false
            listview.isEnabled = true
            btnDiscovery.visibility = View.VISIBLE
        }

        override fun error(message: String?) {}
    }
}