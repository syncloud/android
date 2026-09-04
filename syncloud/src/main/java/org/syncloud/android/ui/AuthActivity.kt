package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.ui.theme.SyncloudTheme

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as SyncloudApplication
        setContent {
            SyncloudTheme {
                AuthScreen(
                    preferences = application.preferences,
                    userService = application.userServiceCached,
                    onSignedIn = {
                        startActivity(Intent(this, DevicesSavedActivity::class.java))
                        finish()
                    },
                    onCredentialsNeeded = { checkExisting ->
                        val intent = Intent(this, AuthCredentialsActivity::class.java)
                        intent.putExtra(AuthConstants.PARAM_CHECK_EXISTING, checkExisting)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun AuthScreen(
    preferences: Preferences,
    userService: IUserService,
    onSignedIn: () -> Unit,
    onCredentialsNeeded: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val email = preferences.redirectEmail
        val password = preferences.redirectPassword
        if (email != null && password != null) {
            busy = true
            val user = withContext(Dispatchers.IO) {
                runCatching { userService.getUser(email, password) }.getOrNull()
            }
            busy = false
            if (user != null) onSignedIn() else onCredentialsNeeded(true)
        }
    }

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp)
                .testTag("auth_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.syncloud_logo),
                contentDescription = stringResource(R.string.logo_image)
            )
            Spacer(Modifier.height(32.dp))
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.testTag("auth_progress"))
            } else {
                Button(
                    onClick = { onCredentialsNeeded(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_in_button")
                ) {
                    Text(stringResource(R.string.action_sign_in))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.${preferences.mainDomain}/register")
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_up_button")
                ) {
                    Text(stringResource(R.string.action_sign_up))
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.build_your_own_server),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://syncloud.org"))
                    )
                }) {
                    Text(stringResource(R.string.learn_more))
                }
            }
        }
    }
}
