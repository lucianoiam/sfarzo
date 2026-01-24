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
import org.androidaudioplugin.composeaudiocontrols.DiatonicKeyboard
import javax.sound.midi.ShortMessage

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
                    Library.sendMidiEvent(ShortMessage(ShortMessage.NOTE_ON, 0, note, 127))
                },
                onNoteOff = { note, _ ->
                    noteOnStates[note] = 0L
                    Library.sendMidiEvent(ShortMessage(ShortMessage.NOTE_OFF, 0, note, 0))
                },
                octaveZeroBased = 3,
                numWhiteKeys = 28,
                totalHeight = 80.dp,
                blackKeyHeight = 48.dp
            )
        }
    }
}
