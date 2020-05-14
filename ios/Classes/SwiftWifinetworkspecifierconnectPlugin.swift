import Flutter
import UIKit

public class SwiftWifinetworkspecifierconnectPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "wifinetworkspecifierconnect", binaryMessenger: registrar.messenger())
    let instance = SwiftWifinetworkspecifierconnectPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
