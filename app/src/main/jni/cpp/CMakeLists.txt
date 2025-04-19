cmake_minimum_required(VERSION 3.4.1)
project("HearWell3")

# <-- Point this to the root of your Superpowered SDK folder
set(PATH_TO_SUPERPOWERED "/absolute/path/to/Superpowered")

include_directories(
        src/main/cpp
        ${PATH_TO_SUPERPOWERED}/OpenSource
)

add_library(
        AudioEngine
        SHARED
        src/main/cpp/AudioEngine.cpp
        ${PATH_TO_SUPERPOWERED}/OpenSource/SuperpoweredAndroidAudioIO.cpp
)

find_library(android-lib android)
find_library(log-lib    log)
find_library(opensles   OpenSLES)

target_link_libraries(
        AudioEngine
        ${android-lib}
        ${log-lib}
        ${opensles}
        ${PATH_TO_SUPERPOWERED}/libSuperpoweredAndroid${ANDROID_ABI}.a
)
