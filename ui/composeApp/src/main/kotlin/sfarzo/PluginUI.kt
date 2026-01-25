// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import juce_cmp.Library
import juce_cmp.renderer.captureFirstFrame

// Make AWT dialogs follow system appearance (dark/light mode)
@Suppress("unused")
private val awtDarkMode = System.setProperty("apple.awt.application.appearance", "system")

/** Wraps content with MaterialTheme and PluginState provider */
@Composable
fun PluginUI(state: PluginState = PluginState(), content: @Composable () -> Unit) {
    MaterialTheme {
        PluginProvider(state = state) {
            content()
        }
    }
}

/** Entry point when running inside JUCE host - handles IPC and frame rendering */
fun HostedPluginUI(content: @Composable () -> Unit) {
    val pluginState = PluginState()

    Library.host(
        // DEV: Uncomment to generate loading_preview.png from first rendered frame
        // onFrameRendered = captureFirstFrame("/tmp/loading_preview.png"),
        onJuceEvent = pluginState::onJuceEvent
    ) {
        PluginUI(pluginState) {
            content()
        }
    }
}
