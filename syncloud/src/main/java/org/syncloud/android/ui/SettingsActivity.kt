package org.syncloud.android.ui

import android.R
import android.content.res.Configuration
import android.preference.PreferenceActivity
import androidx.appcompat.app.AppCompatDelegate
import android.os.Bundle
import org.syncloud.android.ui.SettingsFragment
import android.view.MenuInflater
import android.view.View
import androidx.annotation.LayoutRes
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar

class SettingsActivity : PreferenceActivity() {
    private var mDelegate: AppCompatDelegate? = null
    private val delegate: AppCompatDelegate
        private get() {
            if (mDelegate == null) {
                mDelegate = AppCompatDelegate.create(this, null)
            }
            return mDelegate as AppCompatDelegate
        }

    val supportActionBar: ActionBar? get() = delegate.supportActionBar
    fun setSupportActionBar(toolbar: Toolbar?) = delegate.setSupportActionBar(toolbar)
    override fun getMenuInflater(): MenuInflater = delegate.menuInflater
    override fun setContentView(@LayoutRes layoutResID: Int) = delegate.setContentView(layoutResID)
    override fun setContentView(view: View) = delegate.setContentView(view)
    override fun setContentView(view: View, params: ViewGroup.LayoutParams) = delegate.setContentView(view, params)
    override fun addContentView(view: View, params: ViewGroup.LayoutParams) = delegate.addContentView(view, params)

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
            .replace(R.id.content, SettingsFragment())
            .commit()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate.onPostCreate(savedInstanceState)
    }

    override fun onPostResume() {
        super.onPostResume()
        delegate.onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        delegate.setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        delegate.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    override fun invalidateOptionsMenu() = delegate.invalidateOptionsMenu()
}