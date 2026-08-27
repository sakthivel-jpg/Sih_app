#include <WiFi.h>
#include <WebServer.h>

// ============================================================
// WIFI CONFIGURATION
// ============================================================

const char* WIFI_SSID = "Kani";
const char* WIFI_PASSWORD = "K@123456";

// ============================================================
// DEVICE CONFIGURATION
// ============================================================

const char* DEVICE_NAME = "AGARBATTI-DRYER-01";

WebServer server(80);

// ============================================================
// DEVICE STATE
// ============================================================

String dryingStatus = "READY";
unsigned long dryingStartTime = 0;

// No DHT22 currently connected.
// Therefore these remain unavailable.
float temperature = NAN;
float humidity = NAN;

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
  json += "\"device\":\"";
  json += DEVICE_NAME;
  json += "\",";
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

  json += "\"device\":\"";
  json += DEVICE_NAME;
  json += "\",";

  json += "\"connected\":true,";

  json += "\"temperature\":";
  json += sensorValueToJson(temperature);
  json += ",";

  json += "\"humidity\":";
  json += sensorValueToJson(humidity);
  json += ",";

  json += "\"status\":\"";
  json += dryingStatus;
  json += "\",";

  json += "\"uptime\":";
  json += String(millis() / 1000);

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

  String json = "{";
  json += "\"success\":true,";
  json += "\"status\":\"DRYING\"";
  json += "}";

  server.send(200, "application/json", json);
}

// ============================================================
// POST /drying/stop
// ============================================================

void handleStopDrying() {
  Serial.println("POST /drying/stop");

  dryingStatus = "READY";
  dryingStartTime = 0;

  String json = "{";
  json += "\"success\":true,";
  json += "\"status\":\"READY\"";
  json += "}";

  server.send(200, "application/json", json);
}

// ============================================================
// 404
// ============================================================

void handleNotFound() {
  Serial.print("404: ");
  Serial.println(server.uri());

  String json = "{";
  json += "\"error\":\"Not Found\",";
  json += "\"path\":\"";
  json += server.uri();
  json += "\"";
  json += "}";

  server.send(404, "application/json", json);
}

// ============================================================
// WIFI CONNECTION
// ============================================================

void connectToWiFi() {
  Serial.println();
  Serial.println("Wi-Fi connecting...");

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;

  while (WiFi.status() != WL_CONNECTED && attempts < 60) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("Wi-Fi connected");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());

    Serial.print("SSID: ");
    Serial.println(WiFi.SSID());

    Serial.print("Signal strength: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("Wi-Fi connection failed");
    Serial.println("Check WIFI_SSID and WIFI_PASSWORD");
  }
}

// ============================================================
// HTTP SERVER
// ============================================================

void startHttpServer() {
  server.on("/health", HTTP_GET, handleHealth);
  server.on("/status", HTTP_GET, handleStatus);

  server.on("/drying/start", HTTP_POST, handleStartDrying);
  server.on("/drying/stop", HTTP_POST, handleStopDrying);

  server.onNotFound(handleNotFound);

  server.begin();

  Serial.println("HTTP server started");
  Serial.println("Port: 80");
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

  connectToWiFi();

  if (WiFi.status() == WL_CONNECTED) {
    startHttpServer();

    Serial.println();
    Serial.println("READY FOR ANDROID");
    Serial.print("Open: http://");
    Serial.print(WiFi.localIP());
    Serial.println("/health");
  }
}

// ============================================================
// LOOP
// ============================================================

void loop() {

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Wi-Fi disconnected. Reconnecting...");
    connectToWiFi();

    if (WiFi.status() == WL_CONNECTED) {
      startHttpServer();
    }
  }

  if (WiFi.status() == WL_CONNECTED) {
    server.handleClient();
  }

  delay(10);
}