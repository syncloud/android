package org.syncloud.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.syncloud.android.Preferences
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.ui.theme.SyncloudTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as SyncloudApplication
        setContent {
            SyncloudTheme {
                SettingsScreen(
                    preferences = application.preferences,
                    onSendReport = { application.reportError() },
                    onSignedOut = {
                        val intent = Intent(this, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: Preferences,
    onSendReport: () -> Unit,
    onSignedOut: () -> Unit
) {
    var email by remember { mutableStateOf(preferences.redirectEmail) }
    var mainDomain by remember { mutableStateOf(preferences.mainDomain) }
    var pickingDomain by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen")
        ) {
            SettingsCategory("ACCOUNT")
            ListItem(
                headlineContent = { Text("Email") },
                supportingContent = { Text(email ?: "Not specified yet") },
                modifier = Modifier.testTag("settings_email")
            )
            ListItem(
                headlineContent = {
                    Text(
                        "Sign out from Syncloud",
                        color = if (email == null) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.onSurface
                    )
                },
                supportingContent = { Text("Removes Syncloud account information") },
                modifier = Modifier
                    .clickable(enabled = email != null) {
                        preferences.setCredentials(null, null)
                        email = null
                        onSignedOut()
                    }
                    .testTag("settings_sign_out")
            )

            SettingsCategory("FEEDBACK")
            ListItem(
                headlineContent = { Text("Send log file") },
                supportingContent = { Text("Sends developers application log") },
                modifier = Modifier
                    .clickable { onSendReport() }
                    .testTag("settings_send_log")
            )

            SettingsCategory("ADVANCED")
            ListItem(
                headlineContent = { Text("Server") },
                supportingContent = { Text(mainDomain) },
                modifier = Modifier
                    .clickable { pickingDomain = true }
                    .testTag("settings_server")
            )
        }
    }

    if (pickingDomain) {
        AlertDialog(
            onDismissRequest = { pickingDomain = false },
            title = { Text("Server") },
            text = {
                Column {
                    Preferences.MAIN_DOMAINS.forEach { domain ->
                        DomainOption(
                            selected = domain == mainDomain,
                            label = domain,
                            onSelect = {
                                preferences.setMainDomain(domain)
                                mainDomain = domain
                                pickingDomain = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickingDomain = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DomainOption(selected: Boolean, label: String, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label)
    }
}

@Composable
private fun SettingsCategory(title: String) {
    HorizontalDivider()
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
