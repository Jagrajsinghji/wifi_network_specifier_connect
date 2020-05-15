package com.akaalapps.wifinetworkspecifierconnect;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * WifinetworkspecifierconnectPlugin
 */
public class WifinetworkspecifierconnectPlugin implements FlutterPlugin, StreamHandler, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener, ActivityAware {
    private Context context;

    private final Object initializationLock = new Object();

    private final String TAG = "WIFI_NSCONNECT_PLUGIN";

    private static WifinetworkspecifierconnectPlugin wifiConnectPluginInstance;

    private final int NETWORK_CHANGE_REQUEST = 624;

    private FlutterPluginBinding flutterPluginBinding;
    private ActivityPluginBinding activityPluginBinding;

    private MethodChannel channel;
    private EventChannel eventChannel;
    private Activity activity;

    private MethodCall pendingCall;
    private Result pendingResult;

    private BroadcastReceiver eventReceiver;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
    }

    public static void registerWith(Registrar registrar) {
        if (wifiConnectPluginInstance == null) {
            wifiConnectPluginInstance = new WifinetworkspecifierconnectPlugin();
        }
        wifiConnectPluginInstance.setupPlugin(registrar.messenger(), registrar.context(), registrar.activity(), registrar, null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Log.d("Call", call.method);
        if (call.method.equals("Connect")) {
            if (call.arguments instanceof Map) {
                Log.d("Call", "Asking Persmissions");
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    pendingCall = call;
                    pendingResult = result;
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CHANGE_NETWORK_STATE},
                            NETWORK_CHANGE_REQUEST);

                } else {
                    connectToWifi(call, result);
                }
            } else {
                result.error("Illegal Arguments", "Map Type Needed", "Map Type Needed");
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = null;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == NETWORK_CHANGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCall.method.equals("connect")) {
                    if (pendingCall.arguments instanceof Map) {
                        connectToWifi(pendingCall, pendingResult);

                    } else {
                        pendingResult.error("Illegal Arguments", "Map Type Needed", "Map Type Needed");
                        pendingResult = null;
                        pendingCall = null;
                    }
                }
            } else {
                pendingResult.error(
                        "NO_PERMISION_GRANTED", "Permission CHANGE_NETWORK_STATE is required.", null);
                pendingResult = null;
                pendingCall = null;
            }
            return true;
        }
        return false;

    }

    @Override
    public void onAttachedToActivity(@NotNull ActivityPluginBinding binding) {
        this.activityPluginBinding = binding;
        setupPlugin(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext(), activityPluginBinding.getActivity(), null, activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NotNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);

    }

    @Override
    public void onDetachedFromActivity() {
        tearDown();
    }


    private void setupPlugin(
            final BinaryMessenger messenger,
            final Context context,
            final Activity activity,
            final PluginRegistry.Registrar registrar,
            final ActivityPluginBinding activityBinding) {
        synchronized (initializationLock) {
            Log.i(TAG, "Setup");
            this.activity = activity;
            this.context = context;
            channel = new MethodChannel(messenger, "wifinetworkspecifierconnect");
            eventChannel = new EventChannel(messenger, "wifinetworkspecifierconnect_event");
            channel.setMethodCallHandler(this);
            eventChannel.setStreamHandler(this);

            if (registrar != null) {
                // V1 embedding setupPlugin for activity listeners.
                registrar.addRequestPermissionsResultListener(this);
            } else {
                // V2 embedding setupPlugin for activity listeners.
                activityBinding.addRequestPermissionsResultListener(this);
            }
        }
    }

    private void tearDown() {
        Log.i(TAG, "TearDown");
        context = null;
        activityPluginBinding.removeRequestPermissionsResultListener(this);
        activityPluginBinding = null;
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        channel = null;
        eventChannel = null;
        eventReceiver = null;
    }


    private void connectToWifi(MethodCall call, Result res) {
        WifiDelegate wifiDelegate = new WifiDelegate(call, res, context);
        wifiDelegate.connect();
    }

    @Override
    public void onListen(Object arguments, final EventChannel.EventSink events) {
        eventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                events.success(intent.getStringExtra("Name"));
            }
        };
        context.registerReceiver(eventReceiver, new IntentFilter("SERVICE_EVENT"));
    }

    @Override
    public void onCancel(Object arguments) {
        context.unregisterReceiver(eventReceiver);
        eventReceiver = null;
    }
}
