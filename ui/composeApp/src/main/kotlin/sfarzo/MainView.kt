// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MainView() {
    // TEST: Random level every 1 second
    var testLevel by remember { mutableFloatStateOf(0.7f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            testLevel = Random.nextFloat()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2D2D2D))
    ) {
        Toolbar()
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            VuMeter(
                level = testLevel,
                modifier = Modifier.width(40.dp).height(200.dp)
            )
        }
        Keyboard(modifier = Modifier.fillMaxWidth().aspectRatio(6f))
    }
}