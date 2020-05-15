import 'dart:async';

import 'package:flutter/services.dart';

class Wifinetworkspecifierconnect {
  static final StreamController<bool> _controller = StreamController<bool>();
  static final Stream _state = _controller.stream;
  static const MethodChannel _channel =
      const MethodChannel('wifinetworkspecifierconnect');

  static const EventChannel _event_channel =
      const EventChannel('wifinetworkspecifierconnect_event');

  static Future<String> connect(String ssid, String pass) async {
    final String version = await _channel
        .invokeMethod('Connect', {"SSID": ssid, "PASSWORD": pass});
    _event_channel.receiveBroadcastStream().listen(_onEvent);
    return version;
  }

  static _onEvent(dynamic name) {
    print(name + "  name of event");
    if (name.toString() == "Wifi Avaiable")
      _controller.add(true);
    else
      _controller.add(false);
  }

  static Stream<bool> state() {
    return _state;
  }

  void destroy() {
    _controller.close();
  }
}
