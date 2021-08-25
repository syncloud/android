package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.syncloud.android.Preferences
import org.syncloud.android.Progress
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.Utils.toModels
import org.syncloud.android.core.platform.model.DomainModel
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.tasks.AsyncResult
import org.syncloud.android.tasks.ProgressAsyncTask
import org.syncloud.android.ui.DevicesDiscoveryActivity
import org.syncloud.android.ui.adapters.DevicesSavedAdapter
import java.util.*

class DevicesSavedActivity : AppCompatActivity() {
    private lateinit var listview: ListView
    private var adapter: DevicesSavedAdapter? = null
    private var application: SyncloudApplication? = null
    private var preferences: Preferences? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var btnDiscovery: FloatingActionButton? = null
    private var emptyView: View? = null
    private val progress: Progress = ProgressImpl()

    inner class ProgressImpl : Progress.Empty() {
        override fun start() {
            swipeRefreshLayout!!.isRefreshing = true
            listview!!.isEnabled = false
            btnDiscovery!!.visibility = View.GONE
        }

        override fun stop() {
            swipeRefreshLayout!!.isRefreshing = false
            listview!!.isEnabled = true
            btnDiscovery!!.visibility = View.VISIBLE
        }

        override fun error(message: String?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_devices_saved)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        emptyView = findViewById(android.R.id.empty)
        listview = findViewById(R.id.devices_saved)
        listview.setOnItemClickListener(OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
            val obj = listview.getItemAtPosition(position)
            val domain = obj as DomainModel
            open(domain)
        })
        btnDiscovery = findViewById(R.id.discovery_btn)
        adapter = DevicesSavedAdapter(this)
        listview.setAdapter(adapter)
        application = getApplication() as SyncloudApplication
        preferences = application!!.Preferences
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener { refreshDevices() })
        swipeRefreshLayout.post(Runnable { refreshDevices() })
    }

    private fun refreshDevices() {
        val userService = application!!.userServiceCached()
        val email = preferences!!.redirectEmail
        val password = preferences!!.redirectPassword
        emptyView!!.visibility = View.GONE
        listview!!.emptyView = null
        adapter!!.clear()
        ProgressAsyncTask<Void, User>()
            .setProgress(progress)
            .doWork ( object : ProgressAsyncTask.Work<Void, User> {
                override fun run(vararg args: Void): User {
                    return userService.getUser(email, password)!!
                }
            })
            .onCompleted( object: ProgressAsyncTask.Completed<User> {
                override fun run(result: AsyncResult<User>?) {
                    emptyView!!.visibility = View.VISIBLE
                    listview!!.emptyView = emptyView
                }

            })
            .onSuccess( object: ProgressAsyncTask.Success<User> {
                override fun run(user: User?) {
                    updateUser(user)
                }
            })
            .execute()
    }

    private fun updateUser(user: User?) {
        val domains = toModels(
            user!!.domains!!
        )
        val noDevicesLast = Comparator { first: DomainModel, second: DomainModel ->
            first.name().compareTo(second.name())
        }
        Collections.sort(domains, noDevicesLast)
        adapter!!.clear()
        for (domain in domains) adapter!!.add(domain)
    }

    private fun open(device: DomainModel) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(device.dnsUrl))
        startActivity(browserIntent)
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
}