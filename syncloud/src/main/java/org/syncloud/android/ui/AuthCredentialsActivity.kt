package org.syncloud.android.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.log4j.Logger
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.common.SyncloudResultException
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.model.User

class AuthCredentialsActivity : AppCompatActivity() {
    private lateinit var preferences: Preferences
    private lateinit var userService: IUserService
    private lateinit var emailLoginFormView: LinearLayout
    private lateinit var emailView: EditText
    private lateinit var passwordView: EditText
    private lateinit var signInButton: Button
    private lateinit var progressBar: CircleProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_credentials)
        val application = application as SyncloudApplication
        preferences = application.preferences
        userService = application.userServiceCached
        emailLoginFormView = findViewById<View>(R.id.email_login_form) as LinearLayout
        emailView = findViewById<View>(R.id.email) as EditText
        passwordView = findViewById<View>(R.id.password) as EditText
        passwordView.setOnEditorActionListener(OnEditorActionListener { _, id, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        signInButton = findViewById<View>(R.id.sign_in_button) as Button
        signInButton.setOnClickListener { attemptLogin() }
        progressBar = findViewById<View>(R.id.progress) as CircleProgressBar
        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        val intent = intent
        setTitle(R.string.action_sign_in)
        signInButton.setText(R.string.action_sign_in)

        val redirectEmail = preferences.redirectEmail
        val redirectPassword = preferences.redirectPassword
        if (redirectEmail != null && redirectPassword != null) {
            emailView.setText(redirectEmail)
            passwordView.setText(redirectPassword)
            val checkExisting = intent.getBooleanExtra(AuthConstants.PARAM_CHECK_EXISTING, false)
            if (checkExisting) {
                AlertDialog.Builder(this@AuthCredentialsActivity)
                        .setTitle(getString(R.string.check_credentials))
                        .setMessage(getString(R.string.sign_in_failed))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
        }
    }

    private fun setLayoutEnabled(layout: LinearLayout?, enabled: Boolean) {
        for (i in 0 until layout!!.childCount) {
            val view = layout.getChildAt(i)
            view.isEnabled = enabled
        }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.INVISIBLE
        setLayoutEnabled(emailLoginFormView, !show)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isEmailValid(email: String): Boolean = email.contains("@")
    private fun isPasswordValid(password: String): Boolean = password.length > 4

    private fun validate(): Boolean {
        emailView.error = null
        passwordView.error = null
        val email = emailView.text.toString()
        val password = passwordView.text.toString()
        var hasErrors = false
        var focusView: View? = null
        if (TextUtils.isEmpty(password)) {
            passwordView.error = getString(R.string.error_field_required)
            focusView = passwordView
            hasErrors = true
        } else if (!isPasswordValid(password)) {
            passwordView.error = getString(R.string.error_invalid_password)
            focusView = passwordView
            hasErrors = true
        }
        if (TextUtils.isEmpty(email)) {
            emailView.error = getString(R.string.error_field_required)
            focusView = emailView
            hasErrors = true
        } else if (!isEmailValid(email)) {
            emailView.error = getString(R.string.error_invalid_email)
            focusView = emailView
            hasErrors = true
        }
        if (hasErrors) {
            focusView!!.requestFocus()
            return false
        }
        return true
    }

    private fun attemptLogin() {
        if (!validate())
            return

        val email = emailView.text.toString()
        val password = passwordView.text.toString()
        showProgress(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = doGetUser(email, password)
                withContext(Dispatchers.Main) {
                    showProgress(false)
                    if (user != null) {
                        preferences.setCredentials(email, password)
                        val intent = Intent(this@AuthCredentialsActivity, DevicesSavedActivity::class.java)
                        startActivity(intent)
                    } else {
                        showErrorDialog("User not found")
                    }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    showProgress(false)
                    showError(e)
                }
            }
        }
    }

    private fun doGetUser(email: String, password: String): User? {
        return userService.getUser(email, password)
    }

    private fun showErrorDialog(message: String?) {
        AlertDialog.Builder(this)
                .setTitle("Failed")
                .setMessage(message ?: "Unable to login")
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    private fun getControl(parameter: String?): EditText? {
        if (parameter == "email") return emailView
        return if (parameter == "password") passwordView else null
    }

    private fun showError(error: Throwable) {
        if (error is SyncloudResultException) {
            if (error.result.parameters_messages != null) {
                for (pm in error.result.parameters_messages?: listOf()) {
                    val control = getControl(pm.parameter)
                    if (control != null) {
                        val message = pm.messages?.joinToString("\n")
                        control.error = message
                        control.requestFocus()
                    }
                }
                return
            }
        }
        logger.error("auth error", error)
        showErrorDialog("Wrong password or user does not exist (${error.message})")
    }

    companion object {
        private val logger = Logger.getLogger(AuthCredentialsActivity::class.java)
    }
}