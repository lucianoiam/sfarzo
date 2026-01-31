// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MainView() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2D2D2D))
    ) {
        Toolbar()
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
        }
        Keyboard(modifier = Modifier.fillMaxWidth().aspectRatio(6f))
    }
}