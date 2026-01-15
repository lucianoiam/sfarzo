// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import juce_cmp.Library

fun main(args: Array<String>) {
    Library.init(args)

    if (Library.hasHost) {
        Library.host {
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
