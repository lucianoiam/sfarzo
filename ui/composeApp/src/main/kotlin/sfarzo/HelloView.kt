// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import juce_cmp.Library
import juce_cmp.ipc.JuceValueTree
import org.androidaudioplugin.composeaudiocontrols.DiatonicKeyboard

@Composable
fun HelloView() {
    val noteOnStates = remember { mutableStateListOf(*Array(128) { 0L }) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D2D2D)),
            contentAlignment = Alignment.BottomCenter
        ) {
            DiatonicKeyboard(
                noteOnStates = noteOnStates,
                onNoteOn = { note, _ ->
                    noteOnStates[note] = 1L
                    Library.send(JuceValueTree("noteOn").apply {
                        this["note"] = note
                        this["velocity"] = 127
                    })
                },
                onNoteOff = { note, _ ->
                    noteOnStates[note] = 0L
                    Library.send(JuceValueTree("noteOff").apply {
                        this["note"] = note
                        this["velocity"] = 0
                    })
                },
                octaveZeroBased = 3,
                numWhiteKeys = 28,
                totalHeight = 80.dp,
                blackKeyHeight = 48.dp
            )
        }
    }
}
