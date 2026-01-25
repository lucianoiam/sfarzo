// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

#include "PluginEditor.h"
#include "LoadingPreview.h"

PluginEditor::PluginEditor(PluginProcessor& p)
    : AudioProcessorEditor(&p), processorRef(p)
{
    setSize(800, 400);
    setResizable(true, true);
    setResizeLimits(400, 200, 2048, 1024);

    composeComponent.onProcessReady([this] {
        sendSfzName(processorRef.getLoadedSfzName());
    });

    composeComponent.onEvent([this](const juce::ValueTree& tree) {
        if (tree.getType() == juce::Identifier("action"))
        {
            auto name = tree.getProperty("name").toString();

            // Experimenting with two approaches for file dialogs:
            // 1. JUCE FileChooser (current) - picker runs in host process
            // 2. AWT FileDialog (commented below) - picker runs in Compose process

            if (name == "loadSfz")
            {
                fileChooser = std::make_unique<juce::FileChooser>(
                    "Load SFZ", juce::File(), "*.sfz");
                fileChooser->launchAsync(juce::FileBrowserComponent::openMode
                    | juce::FileBrowserComponent::canSelectFiles,
                    [this](const juce::FileChooser& fc) {
                        auto file = fc.getResult();
                        if (!file.existsAsFile())
                            return;

                        auto loaded = processorRef.loadSfzFile(file);
                        if (loaded.isNotEmpty())
                            sendSfzName(loaded);
                        else
                            sendSfzError("Failed to load " + file.getFileName());
                    });
            }

            // Alternative: AWT FileDialog approach
            // if (name == "loadSfzFile")
            // {
            //     auto path = tree.getProperty("path").toString();
            //     auto file = juce::File(path);
            //     if (!file.existsAsFile())
            //     {
            //         sendSfzError("File not found");
            //         return;
            //     }
            //
            //     auto loaded = processorRef.loadSfzFile(file);
            //     if (loaded.isNotEmpty())
            //         sendSfzName(loaded);
            //     else
            //         sendSfzError("Failed to load " + file.getFileName());
            //
            //     // Restore focus to host window after AWT dialog
            //     juce::Process::makeForegroundProcess();
            // }
        }
    });


    composeComponent.onMidi([this](const juce::MidiMessage& message) {
        processorRef.addMidiFromUI(message);
    });

    // Display captured first frame while Compose UI loads
    // NOTE: Background color should match Compose UI background in HelloView.kt
    composeComponent.setLoadingPreview(
        juce::ImageFileFormat::loadFrom(loading_preview_png, loading_preview_png_len),
        juce::Colour(0xFF2D2D2D));

    addAndMakeVisible(composeComponent);
}

void PluginEditor::paint(juce::Graphics& g)
{
    juce::ignoreUnused(g);
}


void PluginEditor::resized()
{
    composeComponent.setBounds(getLocalBounds());
}

void PluginEditor::sendSfzName(const juce::String& name)
{
    juce::ValueTree tree("sfzLoaded");
    tree.setProperty("name", name, nullptr);
    composeComponent.sendEvent(tree);
}

void PluginEditor::sendSfzError(const juce::String& error)
{
    juce::ValueTree tree("sfzError");
    tree.setProperty("message", error, nullptr);
    composeComponent.sendEvent(tree);
}
