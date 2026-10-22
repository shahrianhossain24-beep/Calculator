package dev.example.calculator.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Palette {
    val Blue = Color(0xFF1F5BFF)
    val LightBlue = Color(0xFF5BC8FF)
    val Gradient = Brush.linearGradient(listOf(Blue, LightBlue))
    val Background = Color(0xFF0B1220)
    val Key = Color(0xFF1A2333)
    val KeyOperator = Color(0x331F5BFF)
    val Muted = Color(0xFF8FA1BD)
    val Error = Color(0xFFFF6B6B)
}
