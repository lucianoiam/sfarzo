// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import juce_cmp.Library
import juce_cmp.ipc.JuceValueTree
import juce_cmp.renderer.captureFirstFrame

object SfzState {
    var name by mutableStateOf("")
    var error by mutableStateOf("")
}

fun main(args: Array<String>) {
    // Make AWT dialogs follow system appearance (dark/light mode)
    System.setProperty("apple.awt.application.appearance", "system")

    Library.init(args)

    if (Library.hasHost) {
        Library.host(
            // DEV: Uncomment to generate loading_preview.png from first rendered frame
            // onFrameRendered = captureFirstFrame("/tmp/loading_preview.png"),
            onJuceEvent = { tree ->
                when (tree.type) {
                    "sfzLoaded" -> {
                        SfzState.name = tree["name"].toStr()
                        SfzState.error = ""
                    }
                    "sfzError" -> {
                        SfzState.error = tree["message"].toStr()
                    }
                }
            }
        ) {
            HelloView()
        }
    } else {
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Sfarzo UI Standalone"
            ) {
                HelloView()
            }
        }
    }
}
