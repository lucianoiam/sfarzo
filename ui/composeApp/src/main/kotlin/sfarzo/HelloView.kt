// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import juce_cmp.Library
import juce_cmp.ipc.JuceValueTree
import org.androidaudioplugin.composeaudiocontrols.DiatonicKeyboard
import javax.sound.midi.ShortMessage

@Composable
fun HelloView() {
    val noteOnStates = remember { mutableStateListOf(*Array(128) { 0L }) }

    MaterialTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D2D2D))
        ) {
            val keyboardHeight = (maxHeight * 0.3f).coerceIn(60.dp, 200.dp)
            val blackKeyHeight = keyboardHeight * 0.6f

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        val tree = JuceValueTree("action")
                        tree["name"] = "loadSfz"
                        Library.sendJuceEvent(tree)
                    }) {
                        Text("Load SFZ...")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (SfzState.error.isNotEmpty()) SfzState.error else SfzState.name,
                        color = if (SfzState.error.isNotEmpty()) Color(0xFFFF6B6B) else Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                DiatonicKeyboard(
                    noteOnStates = noteOnStates,
                    onNoteOn = { note, _ ->
                        noteOnStates[note] = 1L
                        Library.sendMidiEvent(ShortMessage(ShortMessage.NOTE_ON, 0, note, 127))
                    },
                    onNoteOff = { note, _ ->
                        noteOnStates[note] = 0L
                        Library.sendMidiEvent(ShortMessage(ShortMessage.NOTE_OFF, 0, note, 0))
                    },
                    octaveZeroBased = 3,
                    numWhiteKeys = 28,
                    totalHeight = keyboardHeight,
                    blackKeyHeight = blackKeyHeight
                )
            }
        }
    }
}
