#include <jni.h>
#include <android/log.h>
#include "SuperpoweredAndroidAudioIO.h"
#include "SuperpoweredSimple.h"
#include "SuperpoweredFrequencyDomain.h"

static SuperpoweredAndroidAudioIO *audioIO = nullptr;
static SuperpoweredFrequencyDomain *freq = nullptr;
static float *floatBuffer = nullptr;
static float *mag = nullptr, *phase = nullptr;

static bool audioCallback(void *clientdata, short int *audioIOBuffer, int numFrames, int samplerate) {
    SuperpoweredShortIntToFloat(audioIOBuffer, floatBuffer, numFrames);

    if (freq->timeDomainToFrequencyDomain(floatBuffer, mag, phase)) {
        for (int i = 0; i < 512; i++) {
            mag[i] *= 2.0f; // amplify frequencies — real-time gain control
        }
        freq->frequencyDomainToTimeDomain(mag, phase, floatBuffer);
    }

    SuperpoweredFloatToShortInt(floatBuffer, audioIOBuffer, numFrames);
    return true;
}

JNIEXPORT void JNICALL Java_com_yourpackage_AudioEngine_start(JNIEnv *, jobject) {
Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");

floatBuffer = (float *)malloc(1024 * sizeof(float));
mag = (float *)malloc(512 * sizeof(float));
phase = (float *)malloc(512 * sizeof(float));
freq = new SuperpoweredFrequencyDomain(1024);

audioIO = new SuperpoweredAndroidAudioIO(
        48000, 512, true, true, audioCallback, nullptr, -1, SL_ANDROID_STREAM_MEDIA
);
}

JNIEXPORT void JNICALL Java_com_yourpackage_AudioEngine_stop(JNIEnv *, jobject) {
delete audioIO;
delete freq;
free(floatBuffer);
free(mag);
free(phase);
}
