package org.syncloud.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LogoBlue = Color(0xFF00B0F0)
val LogoGreen = Color(0xFF66BD45)

private val LightColors = lightColorScheme(
    primary = LogoBlue,
    secondary = LogoGreen
)

private val DarkColors = darkColorScheme(
    primary = LogoBlue,
    secondary = LogoGreen
)

@Composable
fun SyncloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
