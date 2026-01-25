// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

package sfarzo

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import juce_cmp.Library
import org.androidaudioplugin.composeaudiocontrols.DiatonicKeyboard
import javax.sound.midi.ShortMessage

@Composable
fun Keyboard(modifier: Modifier = Modifier) {
    val noteOnStates = remember { mutableStateListOf(*Array(128) { 0L }) }
    val numWhiteKeys = 28

    BoxWithConstraints(modifier = modifier) {
        val whiteKeyWidth = maxWidth / numWhiteKeys
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
            numWhiteKeys = numWhiteKeys,
            whiteKeyWidth = whiteKeyWidth,
            totalWidth = maxWidth,
            totalHeight = maxHeight,
            blackKeyHeight = maxHeight * 0.6f
        )
    }
}
