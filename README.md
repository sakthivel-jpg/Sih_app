# Agarbatti Dryer System

A Smart Solar-Powered Agarbatti Drying and Compact Packaging System designed for rural women artisans.

## Repository Structure

- `android-app/`: (Virtual placeholder) The Android application source code is currently located at the root of the repository (`app/`, `build.gradle.kts`, etc.).
- `esp32-firmware/`: Contains the Arduino firmware for the ESP32 hardware controller.
- `hardware/`: Future schematic and PCB design files.
- `docs/`: System documentation and setup guides.

## Features
- **Offline-First Android App**: Monitor and control the dryer via Bluetooth Low Energy (BLE) or local Wi-Fi.
- **ESP32 Hardware Controller**: Independent autonomous safety limits and sensor telemetry (DHT22).
- **Persistent Batch History**: Local SQLite (Room) database for tracking drying cycles.
- **Secure Authentication**: Firebase Phone SMS OTP authentication.

## Getting Started

1. **Hardware Setup**: See `docs/ESP32_SETUP.md` for ESP32 flashing instructions.
2. **Android Setup**: Open this folder in Android Studio and run `assembleDebug`. Ensure a valid `google-services.json` is placed in `app/` for Firebase Auth to function.
