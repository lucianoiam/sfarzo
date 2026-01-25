// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import juce_cmp.Library
import juce_cmp.ipc.JuceValueTree
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter

@Composable
fun Toolbar() {
    val pluginState = LocalPluginState.current

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            // Experimenting with two approaches for file dialogs:
            // 1. JUCE FileChooser (current) - picker runs in host process
            // 2. AWT FileDialog (commented below) - picker runs in Compose process
            val tree = JuceValueTree("action")
            tree["name"] = "loadSfz"
            Library.sendJuceEvent(tree)

            // Alternative: AWT file picker
            // showSfzFilePicker()
        }) {
            Text("Load SFZ...")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = pluginState.sfzError.ifEmpty { pluginState.sfzName },
            color = if (pluginState.sfzError.isNotEmpty()) Color(0xFFFF6B6B) else Color.White
        )
    }
}

/*private fun showSfzFilePicker() {
    // Bring Java process to foreground so the dialog gets focus
    java.awt.Desktop.getDesktop().requestForeground(true)

    val dialog = FileDialog(null as Frame?, "Load SFZ", FileDialog.LOAD)
    dialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".sfz", ignoreCase = true) }
    dialog.isVisible = true

    val file = dialog.file
    val dir = dialog.directory
    if (file != null && dir != null) {
        val path = dir + file
        val tree = JuceValueTree("action")
        tree["name"] = "loadSfzFile"
        tree["path"] = path
        Library.sendJuceEvent(tree)
    }
}*/
