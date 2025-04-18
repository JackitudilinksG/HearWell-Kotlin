#include "AudioEngine.h"
#include <SuperpoweredAndroidAudioIO.h>
#include <SuperpoweredSimple.h>
#include <SuperpoweredFrequencyDomain.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>

static SuperpoweredAndroidAudioIO *audioIO = nullptr;
static SuperpoweredFrequencyDomain *freq = nullptr;

// Buffers
static float *floatBuffer = nullptr;   // interleaved stereo floats
static float *leftBuffer = nullptr;    // one channel time‑domain
static float *rightBuffer = nullptr;
static float *mag = nullptr, *phase = nullptr;  // freq‑domain

// User‑supplied gain profiles
static float *userGainLeft = nullptr;
static float *userGainRight = nullptr;
static int userGainSize = 0;

// Audio callback (stereo in & out)
static bool audioCallback(
        void *clientdata, short int *audioIOBuffer, int numFrames, int samplerate
) {
    int numSamples = numFrames * 2;

    // 1) Convert interleaved 16‑bit → interleaved floats
    SuperpoweredShortIntToFloat(audioIOBuffer, floatBuffer, numSamples);

    // 2) Deinterleave
    SuperpoweredDeInterleave(floatBuffer, leftBuffer, rightBuffer, numFrames);

    // 3) Left channel FFT → apply gain → iFFT
    if (freq->timeDomainToFrequencyDomain(leftBuffer, mag, phase)) {
        int bands = (userGainSize < (freq->fftSize>>1)) ? userGainSize : (freq->fftSize>>1);
        for (int i = 0; i < bands; i++) {
            mag[i] *= userGainLeft[i];
        }
        freq->frequencyDomainToTimeDomain(mag, phase, leftBuffer);
    }

    // 4) Right channel FFT → apply gain → iFFT
    if (freq->timeDomainToFrequencyDomain(rightBuffer, mag, phase)) {
        int bands = (userGainSize < (freq->fftSize>>1)) ? userGainSize : (freq->fftSize>>1);
        for (int i = 0; i < bands; i++) {
            mag[i] *= userGainRight[i];
        }
        freq->frequencyDomainToTimeDomain(mag, phase, rightBuffer);
    }

    // 5) Re‑interleave
    SuperpoweredInterleave(leftBuffer, rightBuffer, floatBuffer, numFrames);

    // 6) Convert back to 16‑bit and output
    SuperpoweredFloatToShortInt(floatBuffer, audioIOBuffer, numSamples);

    return true;
}

JNIEXPORT void JNICALL
Java_com_example_hearwell_AudioEngine_setGainProfile(
        JNIEnv *env, jobject, jfloatArray leftGainsArr, jfloatArray rightGainsArr, jint numBands
) {
// Free old arrays
if (userGainLeft)  free(userGainLeft);
if (userGainRight) free(userGainRight);

userGainSize = numBands;
userGainLeft  = (float *)malloc(sizeof(float) * numBands);
userGainRight = (float *)malloc(sizeof(float) * numBands);

env->GetFloatArrayRegion(leftGainsArr,  0, numBands, userGainLeft);
env->GetFloatArrayRegion(rightGainsArr, 0, numBands, userGainRight);
}

JNIEXPORT void JNICALL
Java_com_example_hearwell_AudioEngine_start(JNIEnv *env, jobject) {
// Initialize Superpowered
Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");

const int sampleRate   = 48000;
const int bufferFrames = 512;
const int fftSize      = 1024;

// Allocate all buffers
floatBuffer = (float *)malloc(sizeof(float) * bufferFrames * 2);
leftBuffer  = (float *)malloc(sizeof(float) * bufferFrames);
rightBuffer = (float *)malloc(sizeof(float) * bufferFrames);
mag         = (float *)malloc(sizeof(float) * (fftSize>>1));
phase       = (float *)malloc(sizeof(float) * (fftSize>>1));

// Create FFT processor
freq = new SuperpoweredFrequencyDomain(fftSize);

// Start audio I/O (stereo in/out)
audioIO = new SuperpoweredAndroidAudioIO(
        sampleRate, bufferFrames,
        true,      // enableInput
        true,      // enableOutput
        audioCallback, nullptr,
        -1,        // systemAudioDevice (use default)
        SL_ANDROID_STREAM_MEDIA
);
}

JNIEXPORT void JNICALL
Java_com_example_hearwell_AudioEngine_stop(JNIEnv *env, jobject) {
delete audioIO;
delete freq;

free(floatBuffer);
free(leftBuffer);
free(rightBuffer);
free(mag);
free(phase);

if (userGainLeft)  free(userGainLeft);
if (userGainRight) free(userGainRight);
}
