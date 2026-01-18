// SPDX-FileCopyrightText: 2026 Luciano Iam <oss@lucianoiam.com>
// SPDX-License-Identifier: MIT

// Custom standalone app wrapper that uses native title bar.
// JUCE's default StandaloneFilterWindow doesn't enable it.

#include <juce_audio_utils/juce_audio_utils.h>

extern juce::AudioProcessor* JUCE_CALLTYPE createPluginFilter();

class StandaloneApp : public juce::JUCEApplication
{
public:
    const juce::String getApplicationName() override { return "Sfarzo"; }
    const juce::String getApplicationVersion() override { return "0.1.0"; }

    void initialise(const juce::String&) override
    {
        processor.reset(createPluginFilter());

        deviceManager.initialiseWithDefaultDevices(0, 2);
        player.setProcessor(processor.get());
        deviceManager.addAudioCallback(&player);

        mainWindow = std::make_unique<MainWindow>(getApplicationName(), processor.get());
    }

    void shutdown() override
    {
        mainWindow.reset();
        deviceManager.removeAudioCallback(&player);
        player.setProcessor(nullptr);
        processor.reset();
    }

private:
    class MainWindow : public juce::DocumentWindow
    {
    public:
        MainWindow(const juce::String& name, juce::AudioProcessor* p)
            : DocumentWindow(name,
                             juce::Desktop::getInstance().getDefaultLookAndFeel()
                                 .findColour(ResizableWindow::backgroundColourId),
                             DocumentWindow::allButtons)
        {
            setUsingNativeTitleBar(true);
            setResizable(true, true);
            setResizeLimits(400, 200, 2048, 1024);

            if (auto* editor = p->createEditor())
            {
                setContentOwned(editor, true);
                centreWithSize(editor->getWidth(), editor->getHeight());
            }

            setVisible(true);
        }

        void closeButtonPressed() override
        {
            JUCEApplication::getInstance()->systemRequestedQuit();
        }

    private:
        JUCE_DECLARE_NON_COPYABLE_WITH_LEAK_DETECTOR(MainWindow)
    };

    std::unique_ptr<juce::AudioProcessor> processor;
    juce::AudioDeviceManager deviceManager;
    juce::AudioProcessorPlayer player;
    std::unique_ptr<MainWindow> mainWindow;
};

START_JUCE_APPLICATION(StandaloneApp)
