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

// maximum expected message length
#define MAX_MESSAGE_LENGTH 720

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

// can be tweaked for performance
const int loopResolution = 10;
const int ntpInterval = 60 * 60 * 1000;

// keeping track of check intervals
long ntpCheck = 0;

ESP8266WiFiMulti wifiMulti;
WiFiUDP wifiUDP;
NTP ntp(wifiUDP);

// hold the message data from the meter until it can be sumbitted
char messageData[MAX_MESSAGE_LENGTH + 1];
int messageIndex = 0;

// track the message progress
const char startTag = '/';
const char endTag = '!';
const int checksumLength = 4;
int endTagReceivedAt = -1;
bool messageSubmitted = false;

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

void checkWifi() {
  SERIAL_DEBUG.print("checking WiFi connection..");
  if (wifiMulti.run() != WL_CONNECTED) {
    SERIAL_DEBUG.print(". WiFi disconnected, reconnecting..");
    while (wifiMulti.run() != WL_CONNECTED) {
      SERIAL_DEBUG.print(".");     
    }
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
  SERIAL_DEBUG.print("setting up interval trackers..");

  long now = millis();
  SERIAL_DEBUG.print(".");
  ntpCheck = now - ntpInterval;
  
  SERIAL_DEBUG.println(" done");
}

void updateTime() {
  SERIAL_DEBUG.print("synching with NTP server..");

  ntpCheck = millis();
  ntp.update();

  SERIAL_DEBUG.println(ntp.formattedTime(" current time: %F %T"));
}

void readData() {
  while (SERIAL_INPUT.available()) {
    char data = (char)SERIAL_INPUT.read();
    if (data == startTag) {
      SERIAL_DEBUG.println("start tag received");
  
      messageSubmitted = false;
      endTagReceivedAt = -1;
      messageIndex = 0;
    } else if (data == endTag) {
      SERIAL_DEBUG.println("end tag received");
  
      endTagReceivedAt = messageIndex;
    }
    SERIAL_DEBUG.print(data);
  
    if (!messageSubmitted) {
      if (messageIndex < MAX_MESSAGE_LENGTH) {
        messageData[messageIndex++] = data;
      }
  
      if (endTagReceivedAt > 0 && messageIndex > endTagReceivedAt + checksumLength) {
        messageData[messageIndex++] = '\0';
        SERIAL_DEBUG.println();
        SERIAL_DEBUG.print("message complete at ");
        SERIAL_DEBUG.print(messageIndex);
        SERIAL_DEBUG.println(" characters received");
    
        submitData();
        messageSubmitted = true;
      }
    }
  }
}

void submitData() {
  String data(messageData);
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
  if (messageSubmitted && millis() - ntpCheck >= ntpInterval) {
    updateTime();
  }

  checkWifi();
  readData();
  
  delay(loopResolution);
}
