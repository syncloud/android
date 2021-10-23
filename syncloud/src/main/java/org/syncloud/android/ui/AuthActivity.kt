package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.redirect.IUserService

class AuthActivity : AppCompatActivity() {
    private lateinit var preferences: Preferences
    private lateinit var progressBar: CircleProgressBar
    private lateinit var signInOrOut: LinearLayout
    private lateinit var userService: IUserService
    private lateinit var askCredentialsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        val application = application as SyncloudApplication
        preferences = application.preferences
        userService = application.userServiceCached
        progressBar = findViewById<View>(R.id.progress) as CircleProgressBar
        progressBar.setColorSchemeResources(R.color.logo_blue, R.color.logo_green)
        signInOrOut = findViewById<View>(R.id.sign_in_or_up) as LinearLayout
        val singUpBtn = findViewById<View>(R.id.sign_up_button)
        singUpBtn.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.${preferences.mainDomain}/register")))
        }
        val signInBtn = findViewById<View>(R.id.sign_in_button)
        signInBtn.setOnClickListener{
            startActivity(Intent(this, AuthCredentialsActivity::class.java))
        }
        val learnMoreText = findViewById<View>(R.id.auth_learn_more) as TextView
        learnMoreText.movementMethod = LinkMovementMethod.getInstance()
        askCredentialsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }
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
        progressStart()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userService.getUser(email, password)
                withContext(Dispatchers.Main) {
                    progressStop()
                    if (user != null) {
                        val intent = Intent(this@AuthActivity, DevicesSavedActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        askCredentials()
                    }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    progressStop()
                    askCredentials()
                }
            }
        }
    }

    private fun askCredentials() {
        val intent = Intent(this@AuthActivity, AuthCredentialsActivity::class.java)
        intent.putExtra(AuthConstants.PARAM_CHECK_EXISTING, true)
        startActivity(intent)
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

    private fun progressStop() {
        signInOrOut.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

    private fun progressStart() {
        signInOrOut.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }
}