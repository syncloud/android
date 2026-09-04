package org.syncloud.android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.syncloud.android.Preferences
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.platform.model.DomainModel
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.model.toModels
import org.syncloud.android.ui.theme.SyncloudTheme

class DevicesSavedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as SyncloudApplication
        setContent {
            SyncloudTheme {
                DevicesSavedScreen(
                    preferences = application.preferences,
                    userService = application.userServiceCached
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesSavedScreen(
    preferences: Preferences,
    userService: IUserService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val domains = remember { mutableStateListOf<DomainModel>() }
    var refreshing by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    suspend fun refresh() {
        val email = preferences.redirectEmail
        val password = preferences.redirectPassword
        if (email == null || password == null) return
        refreshing = true
        val loadedDomains = withContext(Dispatchers.IO) {
            val user = runCatching { userService.getUser(email, password) }.getOrNull()
            user?.domains?.toModels().orEmpty().sortedBy { it.name }
        }
        domains.clear()
        domains.addAll(loadedDomains)
        refreshing = false
        loaded = true
    }

    val discoveryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { scope.launch { refresh() } }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_devices)) },
                actions = {
                    TextButton(
                        onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
                        modifier = Modifier.testTag("settings_action")
                    ) {
                        Text(stringResource(R.string.action_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    discoveryLauncher.launch(Intent(context, DevicesDiscoveryActivity::class.java))
                },
                modifier = Modifier.testTag("discovery_button")
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_white_24dp),
                    contentDescription = stringResource(R.string.discovery_button)
                )
            }
        }
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { refresh() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .testTag("devices_saved_screen")
        ) {
            if (domains.isEmpty() && loaded) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_devices_found),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(32.dp)
                            .testTag("devices_saved_empty")
                    )
                }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(domains) { domain ->
                    ListItem(
                        headlineContent = { Text(domain.name) },
                        supportingContent = { Text(domain.title) },
                        modifier = Modifier
                            .clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(domain.dnsUrl()))
                                )
                            }
                            .testTag("device_${domain.name}")
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
