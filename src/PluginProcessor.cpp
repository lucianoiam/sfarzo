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

    // Load SFZ from bundle Resources folder (Contents/Resources/res/)
    auto execDir = juce::File::getSpecialLocation(juce::File::currentExecutableFile)
                       .getParentDirectory();
    auto resDir = execDir.getParentDirectory()  // Contents
                         .getChildFile("Resources")
                         .getChildFile("res");

    sfzFile = resDir.getChildFile("jRhodes3c")
                    .getChildFile("jRhodes3c-looped-flac-sfz")
                    .getChildFile("_jRhodes-stereo-looped.sfz");

    if (sfzFile.existsAsFile())
    {
        DBG("Loading SFZ: " + sfzFile.getFullPathName());
        sfizz_load_file(synth, sfzFile.getFullPathName().toRawUTF8());
        DBG("Loaded " + juce::String(sfizz_get_num_regions(synth)) + " regions");
    }
    else
    {
        DBG("SFZ file not found: " + sfzFile.getFullPathName());
    }
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
    juce::ignoreUnused(midiMessages);
    juce::ScopedNoDenormals noDenormals;

    auto numSamples = buffer.getNumSamples();

    // Render sfizz output
    float* outputs[2] = { buffer.getWritePointer(0), buffer.getWritePointer(1) };
    sfizz_render_block(synth, outputs, 2, numSamples);
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

void PluginProcessor::noteOn(int note, int velocity)
{
    sfizz_send_note_on(synth, 0, note, velocity);
}

void PluginProcessor::noteOff(int note, int velocity)
{
    sfizz_send_note_off(synth, 0, note, velocity);
}

juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter()
{
    return new PluginProcessor();
}
