// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.log10

private const val MIN_DB = -60f

private fun amplitudeToDb(amplitude: Float): Float {
    if (amplitude <= 0f) return 0f
    val db = 20f * log10(amplitude)
    return ((db - MIN_DB) / -MIN_DB).coerceIn(0f, 1f)
}

/**
 * Simple VU meter that displays a vertical bar with smooth decay.
 *
 * @param level Current level (0.0 to 1.0)
 * @param modifier Modifier for sizing/positioning
 * @param color Fill and outline color
 * @param decayMs Time to decay from 1.0 to 0.0
 */
@Composable
fun VuMeter(
    level: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    decayMs: Int = 500
) {
    val animatedLevel = remember { Animatable(0f) }
    val dbLevel = amplitudeToDb(level)

    LaunchedEffect(dbLevel) {
        // Snap to new level, then animate decay to 0
        animatedLevel.snapTo(dbLevel)
        animatedLevel.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = (dbLevel * decayMs).toInt(),
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 2f
        val halfStroke = strokeWidth / 2

        // Outline
        drawRect(
            color = color,
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth)
        )

        // Filled portion (from bottom)
        val fillHeight = (size.height - strokeWidth) * animatedLevel.value.coerceIn(0f, 1f)
        if (fillHeight > 0f) {
            drawRect(
                color = color,
                topLeft = Offset(halfStroke, size.height - halfStroke - fillHeight),
                size = Size(size.width - strokeWidth, fillHeight)
            )
        }
    }
}
