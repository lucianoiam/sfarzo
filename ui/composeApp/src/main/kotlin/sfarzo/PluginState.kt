// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import juce_cmp.ipc.JuceValueTree

/** State received from JUCE host, available via LocalPluginState */
class PluginState {
    var sfzName by mutableStateOf("")
    var sfzError by mutableStateOf("")
    var rmsLevel by mutableStateOf(0f)

    fun onJuceEvent(tree: JuceValueTree) {
        when (tree.type) {
            "sfzLoaded" -> {
                sfzName = tree["name"].toStr()
                sfzError = ""
            }
            "sfzError" -> {
                sfzError = tree["message"].toStr()
            }
            "rmsLevel" -> {
                rmsLevel = tree["level"].toStr().toFloatOrNull() ?: 0f
            }
        }
    }
}

/** Provides PluginState to all composables in the tree */
val LocalPluginState = compositionLocalOf { PluginState() }

/** Wraps content with PluginState provider */
@Composable
fun PluginProvider(state: PluginState = PluginState(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPluginState provides state) {
        content()
    }
}
