#include <Arduino.h>
#include <ESP8266HTTPClient.h>
#include <NTP.h>
#include <WString.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WiFiUdp.h>

#define SERIAL_DEBUG Serial1
#define SERIAL_INPUT Serial
#define SERIAL_RATE 115200
#define DATA_ACTIVATE D5

#define WIFI_SSID ""
#define WIFI_PWD ""
#define NTP_HOST ""

#define HTTP_CONTENT_TYPE_KEY "Content-Type"
#define HTTP_CONTENT_TYPE_VALUE "text/plain"
const String DATA_PROTOCOL = "";
const String DATA_HOST = "";
const String DATA_PATH = "";

const int LOOP_RESOLUTION = 100;
const int NTP_INTERVAL = 60 * 60 * 1000;
const int DATA_INTERVAL = 5 * 1000;

ESP8266WiFiMulti wifiMulti;
WiFiUDP wifiUDP;
NTP ntp(wifiUDP);

long ntpCheck = 0;
long dataCheck = 0;

void initDebug() {
  SERIAL_DEBUG.begin(115200);
  delay(10);
  SERIAL_DEBUG.println();
}

void initWifi() {
  SERIAL_DEBUG.print("initializing WiFi..");
  
  WiFi.mode(WIFI_STA);
  SERIAL_DEBUG.print(".");
  wifiMulti.addAP(WIFI_SSID, WIFI_PWD);
  SERIAL_DEBUG.print(".");
  while (wifiMulti.run() != WL_CONNECTED) {
    SERIAL_DEBUG.print(".");
  }

  SERIAL_DEBUG.print(" connected with IP ");
  SERIAL_DEBUG.println(WiFi.localIP());
}

void initNTP() {
  SERIAL_DEBUG.print("initializing NTP client..");

  ntp.ruleDST("CEST", Last, Sun, Mar, 2, 120);
  SERIAL_DEBUG.print(".");
  ntp.ruleSTD("CET", Last, Sun, Oct, 3, 60);
  SERIAL_DEBUG.print(".");
  ntp.begin(NTP_HOST);

  SERIAL_DEBUG.println(" done");
}

void initMeter() {
  SERIAL_DEBUG.print("initializing serial communication..");
  
  SERIAL_INPUT.begin(SERIAL_RATE);
  if (Serial) {
    SERIAL_DEBUG.println(" done");
  
    SERIAL_DEBUG.print("activating meter data output..");
  
    pinMode(DATA_ACTIVATE, OUTPUT);
    SERIAL_DEBUG.print(".");
    digitalWrite(DATA_ACTIVATE, HIGH);
    SERIAL_DEBUG.println(" done");
  } else {
    SERIAL_DEBUG.println(" failed");
  }
}

void initTrackers() {
  SERIAL_DEBUG.print("setting up..");

  long now = millis();
  SERIAL_DEBUG.print(".");
  ntpCheck = now - NTP_INTERVAL;
  SERIAL_DEBUG.print(".");
  dataCheck = now - DATA_INTERVAL;
  
  SERIAL_DEBUG.println(" done");
}

void updateTime() {
  SERIAL_DEBUG.print("synching with NTP server..");

  ntpCheck = millis();

  ntp.update();

  SERIAL_DEBUG.println(ntp.formattedTime(" current time: %F %T"));
}

String fetchData() {
  dataCheck = millis();
  String result = "";

  if (!Serial) {
    SERIAL_DEBUG.println("serial communication is not initialized");
  }

  if (SERIAL_INPUT.available() > 0) {
    SERIAL_DEBUG.println("receiving data:");
    while (SERIAL_INPUT.available()) {
      char dataChar = char(SERIAL_INPUT.read());
      SERIAL_DEBUG.print(dataChar);
      result += dataChar;
    }
    SERIAL_DEBUG.println();
    SERIAL_DEBUG.print("received ");
    SERIAL_DEBUG.print(result.length());
    SERIAL_DEBUG.println(" characters");
  } else {
    SERIAL_DEBUG.println("no energy data available over serial communication");
  }

  return result;
}

void submitData(String data) {
  if (data.length() > 0) {
    SERIAL_DEBUG.print("submitting energy data..");

    WiFiClient wifi;
    HTTPClient http;
    
    SERIAL_DEBUG.print(".");
    String dataUri = DATA_PROTOCOL + DATA_HOST + DATA_PATH;
    if (http.begin(wifi, dataUri)) {
      SERIAL_DEBUG.print(".");
      http.addHeader(HTTP_CONTENT_TYPE_KEY, HTTP_CONTENT_TYPE_VALUE);
      SERIAL_DEBUG.print(".");
      int httpCode = http.POST(data);
      http.end();
      if (httpCode < 0) {
        SERIAL_DEBUG.print(" failed with error code ");
        SERIAL_DEBUG.print(httpCode);
        SERIAL_DEBUG.print(": ");
        SERIAL_DEBUG.println(http.errorToString(httpCode).c_str());
      } else {
        if (httpCode != HTTP_CODE_OK) {
          SERIAL_DEBUG.print(" failed with HTTP code ");
          SERIAL_DEBUG.println(httpCode);
        } else {
          SERIAL_DEBUG.println(" done");
        }
      }
    } else {
      SERIAL_DEBUG.print(" failed to connect");
    }
  } else {
    SERIAL_DEBUG.println("no energy data to submit");
  }
}

void setup()
{
  initDebug();
  initWifi();
  initNTP();
  initMeter();
  initTrackers();
}

void loop()
{
  long now = millis();
 
  if (now - ntpCheck >= NTP_INTERVAL) {
    updateTime();
  }

  if (now - dataCheck >= DATA_INTERVAL) {
    submitData(fetchData());
  }
  
  delay(LOOP_RESOLUTION);
}
