package org.syncloud.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.common.http.HttpClient
import org.syncloud.android.core.platform.Internal
import org.syncloud.android.core.platform.model.IdentifiedEndpoint
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.ui.theme.SyncloudTheme

const val DISCOVERY_TIMEOUT_SECONDS = 20

class DevicesDiscoveryActivity : ComponentActivity() {
    private lateinit var discoveryManager: DiscoveryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as SyncloudApplication
        discoveryManager = DiscoveryManager(
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager,
            applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        )
        setContent {
            SyncloudTheme {
                DevicesDiscoveryScreen(
                    discoveryManager = discoveryManager,
                    platform = Internal(WebService(HttpClient()))
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        discoveryManager.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesDiscoveryScreen(
    discoveryManager: DiscoveryManager,
    platform: Internal
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val endpoints = remember { mutableStateListOf<IdentifiedEndpoint>() }
    var searching by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    suspend fun discover() {
        endpoints.clear()
        finished = false
        searching = true
        withContext(Dispatchers.IO) {
            discoveryManager.run(DISCOVERY_TIMEOUT_SECONDS) { device ->
                val id = platform.getId(device)
                if (id != null) {
                    withContext(Dispatchers.Main) {
                        if (endpoints.none { it.device == device }) {
                            endpoints.add(IdentifiedEndpoint(device, id))
                        }
                    }
                }
            }
        }
        searching = false
        finished = true
    }

    LaunchedEffect(Unit) { discover() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_activity_discovery)) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scope.launch { discover() } },
                modifier = Modifier.testTag("discovery_refresh_button")
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh_white_24dp),
                    contentDescription = stringResource(R.string.refresh_button)
                )
            }
        }
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = searching,
            onRefresh = { scope.launch { discover() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .testTag("devices_discovery_screen")
        ) {
            if (endpoints.isEmpty() && finished) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_devices_found),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(32.dp)
                            .testTag("devices_discovery_empty")
                    )
                }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(endpoints) { endpoint ->
                    ListItem(
                        headlineContent = { Text(endpoint.id.title ?: endpoint.device) },
                        supportingContent = { Text(endpoint.device) },
                        modifier = Modifier
                            .clickable {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://${endpoint.device}"))
                                )
                            }
                            .testTag("discovered_item")
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
