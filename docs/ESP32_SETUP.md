# ESP32 Setup & Installation Guide

This document details how to flash and configure the ESP32 DevKit for the Agarbatti Dryer system.

## Hardware Connections

| ESP32 Pin | Component       | Description                     |
| :-------- | :-------------- | :------------------------------ |
| **3.3V**  | DHT22 VCC       | Power for DHT22                 |
| **GND**   | DHT22 GND       | Ground for DHT22                |
| **GPIO 4**| DHT22 DATA      | Sensor data line                |
| **GPIO 26**| Dryer Relay IN  | Controls main heating element   |
| **GPIO 27**| Fan Relay IN    | Controls exhaust fan (Optional) |

> [!WARNING]
> **SAFETY FIRST**: Never connect a mains heater directly to an ESP32 GPIO. For initial testing, connect LEDs or low-voltage safe test loads to GPIO 26 and 27 instead of real relays.

## Software Setup (Arduino IDE)

### 1. Arduino IDE Installation
Download and install the latest Arduino IDE from [arduino.cc/en/software](https://www.arduino.cc/en/software).

### 2. ESP32 Board Installation
1. Open Arduino IDE and go to **File > Preferences**.
2. In "Additional Boards Manager URLs", add: 
   `https://dl.espressif.com/dl/package_esp32_index.json`
3. Go to **Tools > Board > Boards Manager**.
4. Search for `esp32` by Espressif Systems and install the package.

### 3. Required Libraries
Go to **Sketch > Include Library > Manage Libraries** and install:
- `DHT sensor library` by Adafruit
- `Adafruit Unified Sensor` by Adafruit

### 4. Uploading Firmware
1. Open `esp32-firmware/agarbatti_dryer/agarbatti_dryer.ino` in Arduino IDE.
2. Go to **Tools > Board** and select `ESP32 Dev Module`.
3. Connect your ESP32 via USB.
4. Go to **Tools > Port** and select your COM port.
5. Click **Upload**. (If it fails to connect, hold the `BOOT` button on the ESP32 when "Connecting..." appears).

### 5. Verification
1. Open **Tools > Serial Monitor**.
2. Set baud rate to `115200`.
3. Verify the ESP32 boots up safely and reports `DRYING STATUS: READY` and correct sensor telemetry.

## Troubleshooting

- **Sensor Error**: Check the DHT22 wiring and ensure a 10k pull-up resistor is used between VCC and DATA if the sensor module doesn't include one.
- **BLE Not Showing**: Ensure you're close to the device. The ESP32 is named `AGARBATTI-DRYER-01`.
- **Compile Errors**: Verify the ESP32 board package is updated to version 2.x.x+.
