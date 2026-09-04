package org.syncloud.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.syncloud.android.Logger
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.common.SyncloudResultException
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.ui.theme.SyncloudTheme

private val logger = Logger.getLogger(AuthCredentialsActivity::class.java)

class AuthCredentialsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as SyncloudApplication
        val checkExisting = intent.getBooleanExtra(AuthConstants.PARAM_CHECK_EXISTING, false)
        setContent {
            SyncloudTheme {
                AuthCredentialsScreen(
                    preferences = application.preferences,
                    userService = application.userServiceCached,
                    checkExisting = checkExisting,
                    onSignedIn = {
                        startActivity(Intent(this, DevicesSavedActivity::class.java))
                        finish()
                    },
                    onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthCredentialsScreen(
    preferences: Preferences,
    userService: IUserService,
    checkExisting: Boolean,
    onSignedIn: () -> Unit,
    onSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf(preferences.redirectEmail ?: "") }
    var password by remember { mutableStateOf(preferences.redirectPassword ?: "") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var dialog by remember {
        mutableStateOf(
            if (checkExisting && preferences.redirectEmail != null) {
                "Sign in with these credentials failed. Please check and correct credentials."
            } else {
                null
            }
        )
    }

    val emailRequired = stringResource(R.string.error_field_required)
    val emailInvalid = stringResource(R.string.error_invalid_email)
    val passwordRequired = stringResource(R.string.error_field_required)
    val passwordInvalid = stringResource(R.string.error_invalid_password)

    fun validate(): Boolean {
        emailError = when {
            email.isEmpty() -> emailRequired
            !email.contains("@") -> emailInvalid
            else -> null
        }
        passwordError = when {
            password.isEmpty() -> passwordRequired
            password.length <= 4 -> passwordInvalid
            else -> null
        }
        return emailError == null && passwordError == null
    }

    fun submit() {
        if (!validate()) return
        busy = true
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching { userService.getUser(email, password) }
            }
            busy = false
            outcome
                .onSuccess { user ->
                    if (user != null) {
                        preferences.setCredentials(email, password)
                        onSignedIn()
                    } else {
                        dialog = "User not found"
                    }
                }
                .onFailure { error ->
                    val messages = (error as? SyncloudResultException)?.result?.parameters_messages
                    if (messages != null) {
                        messages.forEach { pm ->
                            val text = pm.messages?.joinToString("\n")
                            if (pm.parameter == "email") emailError = text
                            if (pm.parameter == "password") passwordError = text
                        }
                    } else {
                        logger.error("auth error", error)
                        dialog = "Wrong password or user does not exist (${error.message})"
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_sign_in)) },
                actions = {
                    TextButton(
                        onClick = onSettings,
                        modifier = Modifier.testTag("settings_action")
                    ) {
                        Text(stringResource(R.string.action_settings))
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp)
                .testTag("credentials_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                enabled = !busy,
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } },
                label = { Text(stringResource(R.string.prompt_email)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_field")
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                enabled = !busy,
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
                label = { Text(stringResource(R.string.prompt_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_field")
            )
            Spacer(Modifier.height(24.dp))
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.testTag("credentials_progress"))
            } else {
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_button")
                ) {
                    Text(stringResource(R.string.action_sign_in))
                }
            }
        }
    }

    dialog?.let { message ->
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.check_credentials)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = { dialog = null },
                    modifier = Modifier.testTag("dialog_ok")
                ) {
                    Text("OK")
                }
            }
        )
    }
}
