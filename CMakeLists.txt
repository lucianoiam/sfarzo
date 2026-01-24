cmake_minimum_required(VERSION 3.22)

project(Sfarzo VERSION 0.1.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Default to Debug build for development
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Debug CACHE STRING "Build type" FORCE)
endif()

#
# 1. Fetch JUCE
#
include(FetchContent)
FetchContent_Declare(
    JUCE
    GIT_REPOSITORY https://github.com/juce-framework/JUCE.git
    GIT_TAG        8.0.4
    GIT_SHALLOW    TRUE
)
FetchContent_MakeAvailable(JUCE)

#
# 2. Fetch juce-cmp (source only, don't process its CMakeLists)
#
FetchContent_Declare(
    juce-cmp
    GIT_REPOSITORY https://github.com/lucianoiam/juce-cmp.git
    GIT_TAG        master
)
FetchContent_Populate(juce-cmp)

# Add juce_cmp module
juce_add_module(${juce-cmp_SOURCE_DIR}/juce_cmp)

#
# 3. Fetch sfizz (library only, no plugins)
#
set(PLUGIN_AU OFF CACHE BOOL "" FORCE)
set(PLUGIN_LV2 OFF CACHE BOOL "" FORCE)
set(PLUGIN_VST3 OFF CACHE BOOL "" FORCE)
set(SFIZZ_JACK OFF CACHE BOOL "" FORCE)
set(SFIZZ_RENDER OFF CACHE BOOL "" FORCE)
set(SFIZZ_SHARED OFF CACHE BOOL "" FORCE)

FetchContent_Declare(
    sfizz
    GIT_REPOSITORY https://github.com/sfztools/sfizz.git
    GIT_TAG        develop
)
set(SFIZZ_SRC_DIR "${FETCHCONTENT_BASE_DIR}/sfizz-src")
set(SFIZZ_BIN_DIR "${FETCHCONTENT_BASE_DIR}/sfizz-build")

if(NOT EXISTS "${SFIZZ_SRC_DIR}/CMakeLists.txt")
    FetchContent_Populate(sfizz)

    # Patch atomic_queue for Clang 17+ compatibility
    # Fix: "a template argument list is expected after a name prefixed by the template keyword"
    set(ATOMIC_QUEUE_FILE "${SFIZZ_SRC_DIR}/external/atomic_queue/include/atomic_queue/atomic_queue.h")
    file(READ "${ATOMIC_QUEUE_FILE}" ATOMIC_QUEUE_CONTENT)
    string(REPLACE "Base::template do_pop_any(" "Base::do_pop_any(" ATOMIC_QUEUE_CONTENT "${ATOMIC_QUEUE_CONTENT}")
    string(REPLACE "Base::template do_push_any(" "Base::do_push_any(" ATOMIC_QUEUE_CONTENT "${ATOMIC_QUEUE_CONTENT}")
    file(WRITE "${ATOMIC_QUEUE_FILE}" "${ATOMIC_QUEUE_CONTENT}")
endif()

add_subdirectory(${SFIZZ_SRC_DIR} ${SFIZZ_BIN_DIR} EXCLUDE_FROM_ALL)

#
# 4. Build Compose UI runtime
#
set(JUCE_CMP_UI_DIR "${juce-cmp_SOURCE_DIR}/juce_cmp_ui")
set(NATIVE_RENDERER_SRC "${JUCE_CMP_UI_DIR}/lib/src/main/cpp/iosurface_renderer.m")
set(NATIVE_RENDERER_OUT "${JUCE_CMP_UI_DIR}/lib/src/main/resources/libiosurface_renderer.dylib")

# Native Metal Renderer Library
if(APPLE)
    add_custom_command(
        OUTPUT "${NATIVE_RENDERER_OUT}"
        COMMAND ${CMAKE_COMMAND} -E make_directory "${JUCE_CMP_UI_DIR}/lib/src/main/resources"
        COMMAND clang -dynamiclib
            -o "${NATIVE_RENDERER_OUT}"
            "${NATIVE_RENDERER_SRC}"
            -framework IOSurface
            -framework Metal
            -framework Foundation
            -fobjc-arc
            -O2
        DEPENDS "${NATIVE_RENDERER_SRC}"
        COMMENT "Building native Metal renderer library"
    )
    add_custom_target(sfarzo_native_renderer DEPENDS "${NATIVE_RENDERER_OUT}")
endif()

# Compose UI Application
set(UI_DIR "${CMAKE_SOURCE_DIR}/ui")

if(WIN32)
    set(GRADLE_CMD cmd /c gradlew.bat)
else()
    set(GRADLE_CMD ./gradlew)
endif()

file(GLOB_RECURSE KOTLIN_SOURCES
    "${UI_DIR}/composeApp/src/*.kt"
    "${JUCE_CMP_UI_DIR}/lib/src/*.kt"
)
list(APPEND KOTLIN_SOURCES "${UI_DIR}/composeApp/build.gradle.kts")

set(UI_STAMP_FILE "${CMAKE_CURRENT_BINARY_DIR}/sfarzo_ui.stamp")

add_custom_command(
    OUTPUT "${UI_STAMP_FILE}"
    COMMAND ${GRADLE_CMD} :composeApp:createDistributable --quiet
    COMMAND ${CMAKE_COMMAND} -E touch "${UI_STAMP_FILE}"
    WORKING_DIRECTORY "${UI_DIR}"
    DEPENDS ${KOTLIN_SOURCES} "${NATIVE_RENDERER_OUT}"
    COMMENT "Building Compose UI"
)

add_custom_target(sfarzo_ui ALL DEPENDS "${UI_STAMP_FILE}")

#
# 5. Build Sfarzo plugin (Standalone + AU)
#
juce_add_plugin(Sfarzo
    COMPANY_NAME "Sfarzo"
    BUNDLE_ID "com.sfarzo.plugin"
    PLUGIN_MANUFACTURER_CODE Sfzo
    PLUGIN_CODE Sfz1
    FORMATS Standalone AU
    PRODUCT_NAME "Sfarzo"
    NEEDS_MIDI_INPUT FALSE
    NEEDS_MIDI_OUTPUT FALSE
    IS_SYNTH TRUE
    IS_MIDI_EFFECT FALSE
    EDITOR_WANTS_KEYBOARD_FOCUS TRUE
)

target_sources(Sfarzo
    PRIVATE
        src/PluginProcessor.cpp
        src/PluginProcessor.h
        src/PluginEditor.cpp
        src/PluginEditor.h
)

target_compile_definitions(Sfarzo
    PUBLIC
        JUCE_WEB_BROWSER=0
        JUCE_USE_CURL=0
        JUCE_VST3_CAN_REPLACE_VST2=0
        JUCE_USE_CUSTOM_PLUGIN_STANDALONE_APP=1
)

# Custom standalone app for native title bar
target_sources(Sfarzo_Standalone PRIVATE src/StandaloneApp.cpp)

target_link_libraries(Sfarzo
    PRIVATE
        juce_cmp
        juce::juce_audio_utils
        sfizz::sfizz
    PUBLIC
        juce::juce_recommended_config_flags
        juce::juce_recommended_lto_flags
        juce::juce_recommended_warning_flags
)

add_dependencies(Sfarzo sfarzo_ui)

# Force relink when UI changes
set_property(TARGET Sfarzo APPEND PROPERTY LINK_DEPENDS "${UI_STAMP_FILE}")

# Platform-specific bundle merging
if(APPLE)
    set(CMP_UI_APP "${CMAKE_SOURCE_DIR}/ui/composeApp/build/compose/binaries/main/app/Sfarzo.app")
    set(CMP_UI_CONTENTS "${CMP_UI_APP}/Contents")

    # Standalone - merge Compose runtime and resources into bundle
    add_custom_command(TARGET Sfarzo_Standalone POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy
            "${CMP_UI_CONTENTS}/MacOS/Sfarzo"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/MacOS/ui"
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CMP_UI_CONTENTS}/app"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/app"
        COMMAND ${CMAKE_COMMAND} -E rename
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/app/Sfarzo.cfg"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/app/ui.cfg"
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CMP_UI_CONTENTS}/runtime"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/runtime"
        COMMAND ${CMAKE_COMMAND} -E copy
            "${JUCE_CMP_UI_DIR}/lib/src/main/resources/libiosurface_renderer.dylib"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/app/libiosurface_renderer.dylib"
        COMMAND ${CMAKE_COMMAND} -E copy_directory_if_different
            "${CMAKE_SOURCE_DIR}/res"
            "$<TARGET_BUNDLE_DIR:Sfarzo_Standalone>/Contents/Resources"
        COMMENT "Merging Compose runtime and resources into Standalone bundle"
    )

    # AU plugin - merge Compose runtime and resources
    add_custom_command(TARGET Sfarzo_AU POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy
            "${CMP_UI_CONTENTS}/MacOS/Sfarzo"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/MacOS/ui"
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CMP_UI_CONTENTS}/app"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/app"
        COMMAND ${CMAKE_COMMAND} -E rename
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/app/Sfarzo.cfg"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/app/ui.cfg"
        COMMAND ${CMAKE_COMMAND} -E copy_directory
            "${CMP_UI_CONTENTS}/runtime"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/runtime"
        COMMAND ${CMAKE_COMMAND} -E copy
            "${JUCE_CMP_UI_DIR}/lib/src/main/resources/libiosurface_renderer.dylib"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/app/libiosurface_renderer.dylib"
        COMMAND ${CMAKE_COMMAND} -E copy_directory_if_different
            "${CMAKE_SOURCE_DIR}/res"
            "$<TARGET_BUNDLE_DIR:Sfarzo_AU>/Contents/Resources"
        COMMENT "Merging Compose runtime and resources into AU bundle"
    )
endif()
