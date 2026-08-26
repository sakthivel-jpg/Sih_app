#include <WiFi.h>
#include <WebServer.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <Preferences.h>
#include <DHT.h>

// --- Pins & Constants ---
#define DHTPIN 4
#define DHTTYPE DHT22
#define RELAY_DRYER 26
#define RELAY_FAN 27

// --- BLE UUIDs ---
#define SERVICE_UUID        "7b7a0001-7b7a-4f8a-9a10-000000000001"
#define CHAR_UUID_STATUS    "7b7a0002-7b7a-4f8a-9a10-000000000002"
#define CHAR_UUID_SENSOR    "7b7a0003-7b7a-4f8a-9a10-000000000003"
#define CHAR_UUID_COMMAND   "7b7a0004-7b7a-4f8a-9a10-000000000004"
#define CHAR_UUID_CONFIG    "7b7a0005-7b7a-4f8a-9a10-000000000005"

// --- Global State ---
enum SystemState { READY, DRYING, STOPPED, COMPLETED, ERROR_STATE };
SystemState currentState = READY;

float currentTemp = 0.0;
float currentHumidity = 0.0;
bool sensorError = false;

// Config
long configDuration = 1800; // 30 mins
float configTargetTemp = 34.0;
float configTargetHum = 45.0;

// Timers
unsigned long dryingStartTime = 0;
unsigned long elapsedSeconds = 0;
unsigned long lastSensorRead = 0;

DHT dht(DHTPIN, DHTTYPE);
Preferences prefs;
WebServer server(80);

// BLE
BLEServer* pServer = NULL;
BLECharacteristic* pStatusChar = NULL;
BLECharacteristic* pSensorChar = NULL;
BLECharacteristic* pConfigChar = NULL;
bool deviceConnected = false;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    }
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      // pServer->startAdvertising(); // restart advertising
    }
};

void saveConfig() {
    prefs.begin("agarbatti", false);
    prefs.putLong("duration", configDuration);
    prefs.putFloat("tgtTemp", configTargetTemp);
    prefs.putFloat("tgtHum", configTargetHum);
    prefs.end();
}

void loadConfig() {
    prefs.begin("agarbatti", true);
    configDuration = prefs.getLong("duration", 1800);
    configTargetTemp = prefs.getFloat("tgtTemp", 34.0);
    configTargetHum = prefs.getFloat("tgtHum", 45.0);
    prefs.end();
}

String getStateString() {
    switch(currentState) {
        case READY: return "READY";
        case DRYING: return "DRYING";
        case STOPPED: return "STOPPED";
        case COMPLETED: return "COMPLETED";
        case ERROR_STATE: return "ERROR";
    }
    return "UNKNOWN";
}

String getSensorJson() {
    return String("{\"temperature\":") + currentTemp + 
           ",\"humidity\":" + currentHumidity + 
           ",\"state\":\"" + getStateString() + "\"" +
           ",\"elapsedSeconds\":" + elapsedSeconds + "}";
}

String getConfigJson() {
    return String("duration=") + configDuration + 
           ",temperature=" + configTargetTemp + 
           ",humidity=" + configTargetHum;
}

void setSafeState() {
    digitalWrite(RELAY_DRYER, LOW);
    digitalWrite(RELAY_FAN, LOW);
}

void processCommand(String cmd) {
    cmd.trim();
    if (cmd == "START_DRYING") {
        if (sensorError) {
            currentState = ERROR_STATE;
        } else {
            currentState = DRYING;
            dryingStartTime = millis();
            elapsedSeconds = 0;
        }
    } else if (cmd == "STOP_DRYING") {
        currentState = STOPPED;
        setSafeState();
    }
    
    // Always notify status change
    if (pStatusChar) {
        pStatusChar->setValue(getStateString().c_str());
        pStatusChar->notify();
    }
}

class MyCommandCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      String value = String(pCharacteristic->getValue().c_str());
      if (value.length() > 0) {
        processCommand(value);
        // Acknowledgement 
        pCharacteristic->setValue("ACK");
      }
    }
};

class MyConfigCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      String value = String(pCharacteristic->getValue().c_str());
      // Expecting: duration=1800,temperature=34.0,humidity=45.0
      // (Basic parsing)
      if (value.indexOf("duration=") != -1) {
          // Parse values (simplified for prototype)
          // In production, use robust tokenizer
      }
      saveConfig();
      pCharacteristic->setValue(getConfigJson().c_str());
      pCharacteristic->notify();
    }
};

void setupBLE() {
    BLEDevice::init("AGARBATTI-DRYER-01");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());

    BLEService *pService = pServer->createService(SERVICE_UUID);

    pStatusChar = pService->createCharacteristic(CHAR_UUID_STATUS, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
    pSensorChar = pService->createCharacteristic(CHAR_UUID_SENSOR, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
    
    BLECharacteristic *pCmdChar = pService->createCharacteristic(CHAR_UUID_COMMAND, BLECharacteristic::PROPERTY_WRITE);
    pCmdChar->setCallbacks(new MyCommandCallbacks());

    pConfigChar = pService->createCharacteristic(CHAR_UUID_CONFIG, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
    pConfigChar->setCallbacks(new MyConfigCallbacks());

    pStatusChar->addDescriptor(new BLE2902());
    pSensorChar->addDescriptor(new BLE2902());

    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    BLEDevice::startAdvertising();
}

void handleApiStatus() { server.send(200, "text/plain", getStateString()); }
void handleApiSensor() { server.send(200, "application/json", getSensorJson()); }
void handleApiStart()  { processCommand("START_DRYING"); server.send(200, "text/plain", "ACK"); }
void handleApiStop()   { processCommand("STOP_DRYING"); server.send(200, "text/plain", "ACK"); }
void handleApiConfig() { server.send(200, "text/plain", getConfigJson()); }

void setupWiFi() {
    // Wi-Fi credentials should ideally be provisioned via BLE Config, 
    // left generic here for the prototype base.
    WiFi.mode(WIFI_STA);
    // WiFi.begin("SSID", "PASS");
    
    server.on("/api/status", HTTP_GET, handleApiStatus);
    server.on("/api/sensor", HTTP_GET, handleApiSensor);
    server.on("/api/start", HTTP_POST, handleApiStart);
    server.on("/api/stop", HTTP_POST, handleApiStop);
    server.on("/api/config", HTTP_GET, handleApiConfig);
    server.begin();
}

void setup() {
    Serial.begin(115200);
    pinMode(RELAY_DRYER, OUTPUT);
    pinMode(RELAY_FAN, OUTPUT);
    setSafeState();
    
    dht.begin();
    loadConfig();
    setupBLE();
    setupWiFi();
}

void loop() {
    unsigned long currentMillis = millis();
    
    // Read sensor every 2 seconds
    if (currentMillis - lastSensorRead > 2000) {
        lastSensorRead = currentMillis;
        float h = dht.readHumidity();
        float t = dht.readTemperature();
        
        if (isnan(h) || isnan(t)) {
            sensorError = true;
            currentState = ERROR_STATE;
            setSafeState();
        } else {
            sensorError = false;
            currentTemp = t;
            currentHumidity = h;
            
            // Hard safety limits independent of Android
            if (currentTemp > 60.0) {
                currentState = ERROR_STATE;
                setSafeState();
            }
        }
        
        if (deviceConnected && pSensorChar) {
            pSensorChar->setValue(getSensorJson().c_str());
            pSensorChar->notify();
        }
    }
    
    if (currentState == DRYING) {
        elapsedSeconds = (currentMillis - dryingStartTime) / 1000;
        
        // Auto-completion
        if (elapsedSeconds >= configDuration) {
            currentState = COMPLETED;
            setSafeState();
            if (pStatusChar) {
                pStatusChar->setValue("COMPLETED");
                pStatusChar->notify();
            }
        } else {
            // Basic thermostat control
            if (currentTemp < configTargetTemp) {
                digitalWrite(RELAY_DRYER, HIGH);
                digitalWrite(RELAY_FAN, HIGH);
            } else {
                digitalWrite(RELAY_DRYER, LOW);
                digitalWrite(RELAY_FAN, LOW);
            }
        }
    }
    
    server.handleClient();
    delay(10);
}
