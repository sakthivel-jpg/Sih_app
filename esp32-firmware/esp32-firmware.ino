#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>

// ============================================================
// DEVICE CONFIGURATION
// ============================================================

const char* DEVICE_NAME = "AGARBATTI-DRYER-01";

Preferences preferences;
WebServer server(80);

// ============================================================
// DEVICE STATE
// ============================================================

String dryingStatus = "READY";
unsigned long dryingStartTime = 0;
float temperature = NAN;
float humidity = NAN;
bool isProvisioningMode = false;

// ============================================================
// HELPERS
// ============================================================

String sensorValueToJson(float value) {
  if (isnan(value)) {
    return "null";
  }
  return String(value, 1);
}

// ============================================================
// GET /health
// ============================================================

void handleHealth() {
  Serial.println("GET /health");
  String json = "{";
  json += "\"device\":\"" + String(DEVICE_NAME) + "\",";
  json += "\"status\":\"OK\"";
  json += "}";
  server.send(200, "application/json", json);
}

// ============================================================
// GET /status
// ============================================================

void handleStatus() {
  Serial.println("GET /status");
  String json = "{";
  json += "\"device\":\"" + String(DEVICE_NAME) + "\",";
  json += "\"connected\":true,";
  json += "\"temperature\":" + sensorValueToJson(temperature) + ",";
  json += "\"humidity\":" + sensorValueToJson(humidity) + ",";
  json += "\"status\":\"" + dryingStatus + "\",";
  json += "\"uptime\":" + String(millis() / 1000);
  json += "}";
  server.send(200, "application/json", json);
}

// ============================================================
// POST /drying/start
// ============================================================

void handleStartDrying() {
  Serial.println("POST /drying/start");
  dryingStatus = "DRYING";
  dryingStartTime = millis();
  String json = "{\"success\":true,\"status\":\"DRYING\"}";
  server.send(200, "application/json", json);
}

// ============================================================
// POST /drying/stop
// ============================================================

void handleStopDrying() {
  Serial.println("POST /drying/stop");
  dryingStatus = "READY";
  dryingStartTime = 0;
  String json = "{\"success\":true,\"status\":\"READY\"}";
  server.send(200, "application/json", json);
}

// ============================================================
// PROVISIONING ENDPOINTS
// ============================================================

void handleProvisionStatus() {
  Serial.println("GET /provision/status");
  String json = "{";
  json += "\"device\":\"" + String(DEVICE_NAME) + "\",";
  json += "\"provisioned\":false,";
  json += "\"mode\":\"PROVISIONING\"";
  json += "}";
  server.send(200, "application/json", json);
}

void handleProvisionWifi() {
  Serial.println("POST /provision/wifi");

  if (server.hasArg("plain") == false) {
    server.send(400, "text/plain", "Body not received");
    return;
  }

  String body = server.arg("plain");
  // Simple JSON parsing to avoid ArduinoJson dependency
  // {"ssid":"...","password":"..."}
  int ssidStart = body.indexOf("\"ssid\":\"") + 8;
  int ssidEnd = body.indexOf("\"", ssidStart);
  int passStart = body.indexOf("\"password\":\"") + 12;
  int passEnd = body.indexOf("\"", passStart);

  if (ssidStart < 8 || ssidEnd == -1 || passStart < 12 || passEnd == -1) {
    server.send(400, "application/json", "{\"success\":false,\"status\":\"INVALID_JSON\"}");
    return;
  }

  String ssidStr = body.substring(ssidStart, ssidEnd);
  String passStr = body.substring(passStart, passEnd);

  const char* ssid = ssidStr.c_str();
  const char* password = passStr.c_str();

  Serial.print("Testing connection to SSID: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("Wi-Fi connected successfully!");
    
    // Save to preferences
    preferences.begin("wifi", false);
    preferences.putString("ssid", ssid);
    preferences.putString("password", password);
    preferences.end();

    String ip = WiFi.localIP().toString();
    String json = "{";
    json += "\"success\":true,";
    json += "\"device\":\"" + String(DEVICE_NAME) + "\",";
    json += "\"status\":\"CONNECTED\",";
    json += "\"ip\":\"" + ip + "\"";
    json += "}";

    server.send(200, "application/json", json);
    
    delay(1000);
    ESP.restart();
  } else {
    Serial.println("Wi-Fi connection failed.");
    WiFi.disconnect();
    
    // Restart SoftAP for another attempt (already running but good to ensure mode is clean)
    // Send failure response
    String json = "{\"success\":false,\"status\":\"WIFI_CONNECTION_FAILED\"}";
    server.send(200, "application/json", json);
  }
}

// ============================================================
// 404
// ============================================================

void handleNotFound() {
  server.send(404, "application/json", "{\"error\":\"Not Found\"}");
}

// ============================================================
// SETUP
// ============================================================

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println();
  Serial.println("================================");
  Serial.println("AGARBATTI DRYER ESP32");
  Serial.println("================================");
  Serial.print("Device: ");
  Serial.println(DEVICE_NAME);

  preferences.begin("wifi", true);
  String ssid = preferences.getString("ssid", "");
  String password = preferences.getString("password", "");
  preferences.end();

  if (ssid == "") {
    // START PROVISIONING MODE
    isProvisioningMode = true;
    String softApSsid = String(DEVICE_NAME) + "-SETUP";
    
    Serial.println("PROVISIONING MODE");
    Serial.println("DEVICE ID:");
    Serial.println(DEVICE_NAME);
    Serial.println("PROVISIONING SSID:");
    Serial.println(softApSsid);
    Serial.println("PROVISIONING IP:");
    Serial.println("192.168.4.1");
    Serial.println("PROVISIONING MODE READY");

    WiFi.mode(WIFI_AP);
    WiFi.softAP(softApSsid.c_str());
    
    server.on("/provision/status", HTTP_GET, handleProvisionStatus);
    server.on("/provision/wifi", HTTP_POST, handleProvisionWifi);
    server.onNotFound(handleNotFound);
    server.begin();

  } else {
    // NORMAL MODE
    Serial.println("Normal Mode - connecting to saved Wi-Fi");
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid.c_str(), password.c_str());

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
      delay(500);
      Serial.print(".");
      attempts++;
    }
    Serial.println();

    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("Wi-Fi connected");
      Serial.print("IP address: ");
      Serial.println(WiFi.localIP());
      Serial.println("HTTP server started");
      Serial.println("READY FOR ANDROID");

      server.on("/health", HTTP_GET, handleHealth);
      server.on("/status", HTTP_GET, handleStatus);
      server.on("/drying/start", HTTP_POST, handleStartDrying);
      server.on("/drying/stop", HTTP_POST, handleStopDrying);
      server.onNotFound(handleNotFound);
      server.begin();
    } else {
      Serial.println("Failed to connect to saved Wi-Fi. Falling back to provisioning mode.");
      preferences.begin("wifi", false);
      preferences.clear();
      preferences.end();
      ESP.restart();
    }
  }
}

// ============================================================
// LOOP
// ============================================================

void loop() {
  server.handleClient();
  delay(10);
}