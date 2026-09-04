package org.syncloud.android.ui

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.syncloud.android.Preferences
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.model.Domain
import org.syncloud.android.core.redirect.model.User
import org.syncloud.android.discovery.DiscoveryManager
import org.syncloud.android.ui.theme.SyncloudTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel5, application = Application::class)
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun preferences(email: String? = null, password: String? = null): Preferences {
        val shared = context.getSharedPreferences("screenshots", Context.MODE_PRIVATE)
        shared.edit().clear().apply()
        val preferences = Preferences(shared)
        preferences.setCredentials(email, password)
        return preferences
    }

    private fun userService(user: User?) = object : IUserService {
        override fun getUser(email: String, password: String): User? = user
    }

    private fun domain(name: String, title: String) = Domain(
        name = name,
        device_name = title.lowercase(),
        device_title = title,
        map_local_address = false,
        web_protocol = "https",
        web_local_port = 443,
        web_port = 443
    )

    private fun capture(name: String) {
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Test
    fun authScreen() {
        compose.setContent {
            SyncloudTheme {
                AuthScreen(
                    preferences = preferences(),
                    userService = userService(null),
                    onSignedIn = {},
                    onCredentialsNeeded = {}
                )
            }
        }
        compose.waitForIdle()
        capture("01-auth")
    }

    @Test
    fun credentialsScreen() {
        compose.setContent {
            SyncloudTheme {
                AuthCredentialsScreen(
                    preferences = preferences(),
                    userService = userService(null),
                    checkExisting = false,
                    onSignedIn = {},
                    onSettings = {}
                )
            }
        }
        compose.waitForIdle()
        capture("02-credentials")
    }

    @Test
    fun devicesSavedScreen() {
        val user = User(
            email = "user@example.com",
            domains = listOf(
                domain("home.syncloud.it", "Home"),
                domain("office.syncloud.it", "Office")
            )
        )
        compose.setContent {
            SyncloudTheme {
                DevicesSavedScreen(
                    preferences = preferences("user@example.com", "password"),
                    userService = userService(user)
                )
            }
        }
        compose.waitForIdle()
        capture("03-devices-saved")
    }

    @Test
    fun devicesSavedEmpty() {
        compose.setContent {
            SyncloudTheme {
                DevicesSavedScreen(
                    preferences = preferences("user@example.com", "password"),
                    userService = userService(User(email = "user@example.com", domains = listOf()))
                )
            }
        }
        compose.waitForIdle()
        capture("04-devices-saved-empty")
    }

    @Test
    fun discoveryEmpty() {
        val discoveryManager = mockk<DiscoveryManager>(relaxed = true)
        compose.setContent {
            SyncloudTheme {
                DevicesDiscoveryScreen(
                    discoveryManager = discoveryManager,
                    platform = mockk(relaxed = true)
                )
            }
        }
        compose.waitForIdle()
        capture("05-discovery-empty")
    }

    @Test
    fun settingsScreen() {
        compose.setContent {
            SyncloudTheme {
                SettingsScreen(
                    preferences = preferences("user@example.com", "password"),
                    onSendReport = {},
                    onSignedOut = {}
                )
            }
        }
        compose.waitForIdle()
        capture("06-settings")
    }
}
