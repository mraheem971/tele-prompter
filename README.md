# 🎙️ TelePrompt Pro — Studio Teleprompter & Video Recorder

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java%2017-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Material Design](https://img.shields.io/badge/UI-Material%203%20XML-7986CB?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![CameraX](https://img.shields.io/badge/Camera-CameraX%201.4-00E5FF)](https://developer.android.com/training/camerax)
[![Database](https://img.shields.io/badge/Database-Room%20(Offline--First)-4285F4?logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20LiveData-00B0FF)](#architecture)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**TelePrompt Pro** is an offline-first, studio-grade teleprompter and video recording application designed for content creators, keynote speakers, journalists, and educators. Engineered with a **Midnight-Dark Theme**, **Electric-Blue Accents**, rounded cards, and responsive controls, it delivers a distraction-free speaking environment with professional camera overlay recording.

---

## ✨ Features

### 🎛️ 1. Home Dashboard & Speaking Metrics
* **Real-Time Speech Metrics**: Instant calculations of total scripts, cumulative word count, and estimated speaking duration at average conversational pace (~130 WPM).
* **Quick Action Launchers**: 1-tap jump to distraction-free reading or studio video recording.
* **Recent Scripts Carousel**: Rapidly access, prompt, or record your most recent scripts.
* **Pre-bundled Creator Templates**: Quick-start scripts for YouTube videos, elevator pitches, keynote speeches, viral shorts/reels, and executive interviews.

### 📜 2. Distraction-Free Teleprompter Reading Mode
* **Smooth Auto-Scroll Engine**: Jitter-free ~60fps smooth scrolling with customizable velocity (1x to 10x).
* **Mirror Mode (Beam-Splitter Support)**:
  * Horizontal flip (`scaleX = -1f`) for teleprompter glass rigs.
  * Vertical flip (`scaleY = -1f`) for ceiling-mounted studio screens.
* **Reading Focus Guide Line**: Horizontal electric-blue highlight band to anchor eye contact.
* **Animated Countdown Timer**: Pulsing 3-2-1-GO! countdown overlay before auto-scrolling begins.
* **Live Customization Sheet**:
  * Font size slider (`18sp` to `72sp`).
  * Line spacing multiplier (`1.0x` to `2.4x`).
  * Reading width slider (`40%` to `100%`) to keep gaze centered near the camera.
  * 4 high-contrast color presets: White, Cyan, Yellow, Green.
* **Hardware Volume Keys**: Use physical Volume Up/Down keys to adjust speed on the fly.
* **Tap-to-Hide HUD**: Tap anywhere to toggle the floating control dock for full immersion.

### 🎥 3. Studio CameraX Video Recorder
* **CameraX Video Capture**: Records video with synchronized audio in Full HD (1080p), HD (720p), or SD (480p).
* **Lens-Aligned Prompter Overlay**: Semi-transparent prompter card placed near the camera lens to minimize eye drift.
* **Adjustable Overlay Opacity**: Quick-cycle opacity (40%, 75%, 90%, 100%).
* **Camera Controls**:
  * 1-tap front/back camera flip.
  * Flashlight / Torch toggle for rear camera recording.
  * Independent prompt play/pause and reset during recording.
* **Studio REC Indicator**: Pulsing red record badge with real-time `HH:MM:SS` timer.

### 🎬 4. Video Preview, Export & Sharing
* **Instant In-App Playback**: Embedded `VideoView` with seek controls.
* **Metadata Inspector**: Inspect duration, file size, and file path.
* **Secure Sharing**: Uses Android `FileProvider` to share directly to YouTube, Drive, WhatsApp, etc.
* **Gallery Export**: Export MP4 video directly to the system `Movies/Teleprompter` gallery via `MediaStore`.

### ✍️ 5. Script Editor & Library
* **Live Writing Stats**: Dynamic word count and speaking time estimate as you type.
* **Formatting Assists**: Quick buttons for `[PAUSE / BREATHE]` cue markers, bullet points `•`, and text clear.
* **Library Organization**: Search bar with live query filtering and category filter chips (*All*, *YouTube*, *Keynote*, *Pitch*, *Shorts/Reels*, *Interview*).
* **Script Management**: Edit, duplicate, share as plain text, or delete with confirmation.

### ⚙️ 6. Settings & Themes
* **App Theme**: Midnight Dark (default), Modern Light, or Follow System.
* **Defaults Customization**: Configure default scroll speed, font size, countdown timer, mirror mode, and camera quality.

---

## 🏗️ Architecture

TelePrompt Pro is built following modern **MVVM (Model-View-ViewModel)** guidelines and **Offline-First** principles:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (XML Views)                 │
│  MainActivity  •  HomeFragment  •  LibraryFragment     │
│  PrompterActivity • CameraRecordingActivity • Preview  │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│  MainViewModel   •  EditorViewModel  •  PrompterVM     │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                   Repository Layer                     │
│        ScriptRepository  •  PreferencesRepository      │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                   Local Data Source                    │
│      Room Database (SQLite)  •  SharedPreferences       │
└────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack & Dependencies

| Component | Library / Framework |
| :--- | :--- |
| **Language** | Java 17 |
| **Min / Target SDK** | Min SDK 24 (Android 7.0) / Target SDK 37 (Android 15+) |
| **UI Components** | Google Material Design 3 (`com.google.android.material:material`) |
| **Camera Framework**| AndroidX CameraX 1.4 (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-video`, `camera-view`) |
| **Local Database** | AndroidX Room 2.6 (`room-runtime`, `room-compiler`) |
| **Architecture** | AndroidX Lifecycle (`viewmodel`, `livedata`, `activity-ktx`) |
| **Concurrency** | Google Guava (`guava:33.3.1-android`) & Java ExecutorService |
| **File Sharing** | AndroidX Core `FileProvider` & MediaStore API |

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug / Meerkat** (or newer)
* **JDK 17** or **JDK 21**
* Android device or emulator running **API 24 or higher**

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/mraheem971/tele-prompter.git

# Navigate to project directory
cd tele-prompter

# Build the Debug APK
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permissions

The application requests only necessary permissions for recording and sharing:
* `android.permission.CAMERA`: Live camera preview and video capture.
* `android.permission.RECORD_AUDIO`: High-fidelity audio recording.
* `android.permission.WRITE_EXTERNAL_STORAGE` (API ≤ 28): For legacy storage export.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
