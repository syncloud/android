package org.syncloud.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar
import org.syncloud.android.Preferences
import org.syncloud.android.Progress
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.tasks.AsyncResult
import org.syncloud.android.tasks.ProgressAsyncTask
import org.syncloud.android.tasks.ProgressAsyncTask.Completed
import org.syncloud.android.tasks.ProgressAsyncTask.Work

const val REQUEST_AUTHENTICATE = 1

class AuthActivity : Activity() {
    private lateinit var preferences: Preferences
    private lateinit var progressBar: CircleProgressBar
    private lateinit var signInOrOut: LinearLayout
    private lateinit var userService: IUserService
    private val progress: Progress = ProgressImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val application = application as SyncloudApplication
        preferences = application.preferences
        userService = application.userServiceCached
        progressBar = findViewById<View>(R.id.progress) as CircleProgressBar
        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        signInOrOut = findViewById<View>(R.id.sign_in_or_up) as LinearLayout
        val learnMoreText = findViewById<View>(R.id.auth_learn_more) as TextView
        learnMoreText.movementMethod = LinkMovementMethod.getInstance()
        proceedWithLogin()
    }

    private fun proceedWithLogin() {
        val redirectEmail = preferences.redirectEmail
        val redirectPassword = preferences.redirectPassword
        if (redirectEmail != null && redirectPassword != null) {
            login(redirectEmail, redirectPassword)
        }
    }

    private fun login(email: String, password: String) {
        ProgressAsyncTask<Void, User?>()
            .setProgress(progress)
            .doWork(object : Work<Void, User?> {
                override fun run(vararg args: Void): User? {
                    return userService.getUser(email, password)
                }
            })
            .onCompleted(object : Completed<User?> {
                override fun run(result: AsyncResult<User?>?) {
                    onLoginCompleted(result)
                }
            })
            .execute()
    }

    private fun onLoginCompleted(result: AsyncResult<User?>?) {
        if (result?.hasValue() == true) {
            val intent = Intent(this@AuthActivity, DevicesSavedActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@AuthActivity, AuthCredentialsActivity::class.java)
            intent.putExtra(AuthConstants.PARAM_CHECK_EXISTING, true)
            startActivityForResult(intent, REQUEST_AUTHENTICATE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    fun signIn(view: View?) {
        val credentialsIntent = Intent(this, AuthCredentialsActivity::class.java)
        startActivityForResult(credentialsIntent, REQUEST_AUTHENTICATE)
    }

    fun signUp(view: View?) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.${preferences.mainDomain}/register")))
    }

    inner class ProgressImpl : Progress.Empty() {
        override fun start() {
            signInOrOut.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }

        override fun stop() {
            signInOrOut.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }
}