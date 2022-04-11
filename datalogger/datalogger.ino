#include <Arduino.h>
#include <ESP8266HTTPClient.h>
#include <NTP.h>
#include <WString.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WiFiUdp.h>

// serial and pin setup for a NodeMCU ESP-12
#define SERIAL_DEBUG Serial1
#define SERIAL_INPUT Serial
#define DATA_ACTIVATE D5

// hard-coded baud rate for the Sagemcom meter
#define SERIAL_RATE 115200

// local configuration parameters should be defined in localconfig.h
#if __has_include("localconfig.h")
#include "localconfig.h"
#endif

#ifndef WIFI_SSID
#define WIFI_SSID "ssid"
#endif
#ifndef WIFI_PWD
#define WIFI_PWD "pwd"
#endif
#ifndef NTP_HOST
#define NTP_HOST "se.pool.ntp.org"
#endif
#ifndef DATA_PROTOCOL
#define DATA_PROTOCOL "https://"
#endif
#ifndef DATA_HOST
#define DATA_HOST "data.host.invalid"
#endif
#ifndef DATA_PATH
#define DATA_PATH "/meter/collector/submit"
#endif

#define HTTP_CONTENT_TYPE_KEY "Content-Type"
#define HTTP_CONTENT_TYPE_VALUE "text/plain"

const String dataProtocol = DATA_PROTOCOL;
const String dataHost = DATA_HOST;
const String dataPath = DATA_PATH;

// can be tweaked for performance and/or reliability
const int loopResolution = 100;
const int ntpInterval = 60 * 60 * 1000;
const int dataInterval = 60 * 1000;

// keeping track of check intervals
long ntpCheck = 0;
long dataCheck = 0;

ESP8266WiFiMulti wifiMulti;
WiFiUDP wifiUDP;
NTP ntp(wifiUDP);

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

void requestData(bool enable) {

  if (enable) {
    SERIAL_DEBUG.print("activating meter data output..");
    digitalWrite(DATA_ACTIVATE, HIGH);
  } else {
    SERIAL_DEBUG.print("deactivating meter data output..");
    digitalWrite(DATA_ACTIVATE, LOW);
  }

  SERIAL_DEBUG.println(" done");
}

void initMeter() {
  SERIAL_DEBUG.print("initializing serial communication..");
  
  SERIAL_INPUT.begin(SERIAL_RATE);
  if (SERIAL_INPUT) {
    pinMode(DATA_ACTIVATE, OUTPUT);

    SERIAL_DEBUG.println(" done");

    requestData(false);
  } else {
    SERIAL_DEBUG.println(" failed");
  }
}

void initTrackers() {
  SERIAL_DEBUG.print("setting up..");

  long now = millis();
  SERIAL_DEBUG.print(".");
  ntpCheck = now - ntpInterval;
  SERIAL_DEBUG.print(".");
  dataCheck = now - dataInterval;
  
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

  requestData(true);

  SERIAL_DEBUG.print("waiting for data..");
  int count = 0;
  while (!SERIAL_INPUT.available()) {
    SERIAL_DEBUG.print(".");
    if (count++ > 100) {
      SERIAL_DEBUG.println(" no energy data available over serial communication");
      requestData(false);
      return "";
    }
    delay(10);
  }
  SERIAL_DEBUG.println(" data available");

  SERIAL_DEBUG.println("receiving data.. ");
  while (SERIAL_INPUT.available()) {
    char dataChar = char(SERIAL_INPUT.read());
    SERIAL_DEBUG.print(dataChar);
    result += dataChar;
  }
  SERIAL_DEBUG.println();
  SERIAL_DEBUG.print("received ");
  SERIAL_DEBUG.print(result.length());
  SERIAL_DEBUG.println(" characters");

  requestData(false);
  return result;
}

void submitData(String data) {
  if (data.length() > 0) {
    SERIAL_DEBUG.print("submitting energy data..");

    WiFiClient wifi;
    HTTPClient http;
    
    SERIAL_DEBUG.print(".");
    String dataUri = dataProtocol + dataHost + dataPath;
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
  if (SERIAL_INPUT.available()) {
    SERIAL_DEBUG.println("receiving spam:");
    while (SERIAL_INPUT.available()) {
      SERIAL_DEBUG.print((char)SERIAL_INPUT.read());
    }
    SERIAL_DEBUG.println();
  }

  long now = millis();
 
  if (now - ntpCheck >= ntpInterval) {
    updateTime();
  }

  if (now - dataCheck >= dataInterval) {
    submitData(fetchData());
  }

  delay(loopResolution);
}
