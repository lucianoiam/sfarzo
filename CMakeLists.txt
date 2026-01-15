cmake_minimum_required(VERSION 3.22)

project(Sfarzo VERSION 0.1.0)

include(FetchContent)

FetchContent_Declare(
    juce-cmp
    GIT_REPOSITORY https://github.com/lucianoiam/juce-cmp.git
    GIT_TAG        main
)

FetchContent_MakeAvailable(juce-cmp)
