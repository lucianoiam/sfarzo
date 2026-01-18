// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

#include "PluginEditor.h"

PluginEditor::PluginEditor(PluginProcessor& p)
    : AudioProcessorEditor(&p), processorRef(p)
{
    setSize(800, 400);
    setResizable(true, true);
    setResizeLimits(400, 200, 2048, 1024);

    composeComponent.onFirstFrame([this] {
        uiReady = true;
        repaint();
    });

    composeComponent.onEvent([this](const juce::ValueTree& tree) {
        handleUIEvent(tree);
    });

    addAndMakeVisible(composeComponent);
}

void PluginEditor::paint(juce::Graphics& g)
{
    juce::ignoreUnused(g);
}

void PluginEditor::paintOverChildren(juce::Graphics& g)
{
    if (uiReady)
        return;

    g.fillAll(juce::Colour(0xFF2D2D2D));
    g.setColour(juce::Colour(0xFF888888));
    g.setFont(juce::FontOptions(15.0f));
    g.drawFittedText("Starting UI...", getLocalBounds(), juce::Justification::centred, 1);
}

void PluginEditor::resized()
{
    composeComponent.setBounds(getLocalBounds());
}

void PluginEditor::handleUIEvent(const juce::ValueTree& tree)
{
    auto type = tree.getType().toString();

    if (type == "noteOn")
    {
        int note = tree.getProperty("note", 0);
        int velocity = tree.getProperty("velocity", 127);
        processorRef.noteOn(note, velocity);
    }
    else if (type == "noteOff")
    {
        int note = tree.getProperty("note", 0);
        int velocity = tree.getProperty("velocity", 0);
        processorRef.noteOff(note, velocity);
    }
}
