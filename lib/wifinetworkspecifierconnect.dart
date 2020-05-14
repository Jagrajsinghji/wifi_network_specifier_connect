import 'dart:async';

import 'package:flutter/services.dart';

class Wifinetworkspecifierconnect {
  static const MethodChannel _channel =
      const MethodChannel('wifinetworkspecifierconnect');

  static Future<String> connect(String ssid, String pass) async {
    final String version = await _channel
        .invokeMethod('Connect', {"SSID": ssid, "PASSWORD": pass});
    return version;
  }
}
