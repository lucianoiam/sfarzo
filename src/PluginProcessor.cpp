// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

#include "PluginProcessor.h"
#include "PluginEditor.h"
#include <sfizz.h>

PluginProcessor::PluginProcessor()
    : AudioProcessor(BusesProperties()
                     .withOutput("Output", juce::AudioChannelSet::stereo(), true))
{
    synth = sfizz_create_synth();

    // Load default SFZ from bundle Resources folder
    auto execDir = juce::File::getSpecialLocation(juce::File::currentExecutableFile)
                       .getParentDirectory();
    auto resDir = execDir.getParentDirectory()  // Contents
                         .getChildFile("Resources");

    auto defaultSfz = resDir.getChildFile("jRhodes3c")
                            .getChildFile("jRhodes3c-looped-flac-sfz")
                            .getChildFile("_jRhodes-stereo-looped.sfz");

    if (defaultSfz.existsAsFile())
        loadSfzFile(defaultSfz);
}

PluginProcessor::~PluginProcessor()
{
    if (synth)
        sfizz_free(synth);
}

const juce::String PluginProcessor::getName() const
{
    return JucePlugin_Name;
}

bool PluginProcessor::acceptsMidi() const
{
    return true;
}

bool PluginProcessor::producesMidi() const
{
    return false;
}

bool PluginProcessor::isMidiEffect() const
{
    return false;
}

double PluginProcessor::getTailLengthSeconds() const
{
    return 0.0;
}

int PluginProcessor::getNumPrograms()
{
    return 1;
}

int PluginProcessor::getCurrentProgram()
{
    return 0;
}

void PluginProcessor::setCurrentProgram(int index)
{
    juce::ignoreUnused(index);
}

const juce::String PluginProcessor::getProgramName(int index)
{
    juce::ignoreUnused(index);
    return {};
}

void PluginProcessor::changeProgramName(int index, const juce::String& newName)
{
    juce::ignoreUnused(index, newName);
}

void PluginProcessor::prepareToPlay(double sampleRate, int samplesPerBlock)
{
    sfizz_set_sample_rate(synth, static_cast<float>(sampleRate));
    sfizz_set_samples_per_block(synth, samplesPerBlock);
}

void PluginProcessor::releaseResources()
{
}

bool PluginProcessor::isBusesLayoutSupported(const BusesLayout& layouts) const
{
    if (layouts.getMainOutputChannelSet() != juce::AudioChannelSet::mono()
        && layouts.getMainOutputChannelSet() != juce::AudioChannelSet::stereo())
        return false;

    return true;
}

void PluginProcessor::processBlock(juce::AudioBuffer<float>& buffer, juce::MidiBuffer& midiMessages)
{
    juce::ScopedNoDenormals noDenormals;

    // Merge UI MIDI into host MIDI buffer
    {
        std::lock_guard<std::mutex> lock(uiMidiMutex);
        midiMessages.addEvents(uiMidiBuffer, 0, buffer.getNumSamples(), 0);
        uiMidiBuffer.clear();
    }

    // Process MIDI and send to sfizz
    for (const auto metadata : midiMessages)
    {
        auto message = metadata.getMessage();
        if (message.isNoteOn())
            sfizz_send_note_on(synth, 0, message.getNoteNumber(), message.getVelocity());
        else if (message.isNoteOff())
            sfizz_send_note_off(synth, 0, message.getNoteNumber(), message.getVelocity());
    }

    // Render sfizz output
    auto numSamples = buffer.getNumSamples();
    float* outputs[2] = { buffer.getWritePointer(0), buffer.getWritePointer(1) };
    sfizz_render_block(synth, outputs, 2, numSamples);

    // Calculate RMS for metering (max of both channels)
    float rawRms = 0.0f;
    for (int ch = 0; ch < buffer.getNumChannels(); ++ch)
        rawRms = std::max(rawRms, buffer.getRMSLevel(ch, 0, numSamples));

    // Apply single-pole low-pass filter for smooth metering
    // smoothedRms = coeff * smoothedRms + (1 - coeff) * rawRms
    smoothedRms = rmsSmoothingCoeff * smoothedRms + (1.0f - rmsSmoothingCoeff) * rawRms;
    rmsLevel.store(smoothedRms);
}

bool PluginProcessor::hasEditor() const
{
    return true;
}

juce::AudioProcessorEditor* PluginProcessor::createEditor()
{
    return new PluginEditor(*this);
}

void PluginProcessor::getStateInformation(juce::MemoryBlock& destData)
{
    juce::ignoreUnused(destData);
}

void PluginProcessor::setStateInformation(const void* data, int sizeInBytes)
{
    juce::ignoreUnused(data, sizeInBytes);
}

juce::String PluginProcessor::loadSfzFile(const juce::File& file)
{
    if (!file.existsAsFile())
        return {};

    if (!sfizz_load_file(synth, file.getFullPathName().toRawUTF8()))
    {
        loadedSfzName = {};
        return {};
    }

    loadedSfzName = file.getFileNameWithoutExtension();
    return loadedSfzName;
}

void PluginProcessor::addMidiFromUI(const juce::MidiMessage& message)
{
    std::lock_guard<std::mutex> lock(uiMidiMutex);
    uiMidiBuffer.addEvent(message, 0);
}

juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter()
{
    return new PluginProcessor();
}
