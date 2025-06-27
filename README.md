# Hear Well
> **Author: Deric Jojo**

## 📄 Description
**Hear Well** is an Android application built with Kotlin that transforms your regular earbuds or earphones into intelligent hearing aids. Designed with accessibility and customization in mind, the app processes real-time audio based on individual hearing profiles and adapts to various listening environments.

### 🎧 Real‑Time Hearing Aid Technology
Leveraging **AAudio** and **Oboe**, this app enables:
- Real‑time capture and playback of environmental sounds.
- Frequency‑specific audio adjustments based on the user’s hearing test results.
- Enhanced audio experience through personalized amplification for each ear.
- Use of any connected earphones or earbuds for audio output.

### 👂 Hearing Test Integration
Before beginning real‑time audio processing, users can:
- Take an **in‑app hearing test** that identifies their hearing sensitivity across key frequency bands.
- Have their unique hearing profile saved and used to calibrate sound in real time.
- Re‑run tests anytime to update their profile as needed.

### 🌍 Environmental Presets
The app includes environment‑based presets that can be activated on the go:
- **Quiet Room**: Emphasizes clarity and soft speech.
- **Noisy Street**: Reduces background noise while enhancing speech.
- **Music Mode**: Preserves audio quality for musical content.
- Users can create and save **custom profiles** for different situations.

### 🗣️ Text‑to‑Speech (TTS) and Speech‑to‑Text (STT)
The app also includes accessibility features using **native Android APIs**:
- Convert spoken words into text (STT).
- Read text out loud using customizable pitch and speed (TTS).
- Translate text into other languages using **LibreTranslate**.

## 🔨 How to Install and Run the Project
Clone this repository:
```bash
git clone https://github.com/yourusername/HearWell.git
```
## Project Structure
```bash
com.hearwell.aid               # Root Package
|
├── audio                      # Audio processing logic using AAudio/Oboe
│   ├── RealtimeProcessor      # Core class for frequency‑based audio manipulation
│   └── EnvironmentProfiles    # Presets for real‑world listening scenarios
|
├── screens                    # UI screens
│   ├── HearingTestScreen      # Hearing test interface
│   ├── RealTimeAudioScreen    # Main hearing aid interface
│   ├── STTScreen              # Speech‑to‑Text functionality
│   └── TTSScreen              # Text‑to‑Speech functionality
|
├── firebase                   # Firebase integration
│   └── UserProfileManager     # Stores hearing test data
|
├── translate                  # LibreTranslate integration
│   └── Translator             # Handles text translation
|
├── theme                      # App theming and styling
│   └── Colors, Typography, etc.
|
└── MainActivity               # Main launcher activity
```
