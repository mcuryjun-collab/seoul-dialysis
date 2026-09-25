package com.seoul.dialysis.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E99),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFE9FF),
    secondary = Color(0xFF00687A),
    background = Color(0xFFF7F9FC),
    surface = Color.White
)

@Composable
fun SeoulDialysisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), content = content)
}
