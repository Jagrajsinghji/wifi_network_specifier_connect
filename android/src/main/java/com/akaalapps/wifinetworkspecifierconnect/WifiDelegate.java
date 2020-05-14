package com.akaalapps.wifinetworkspecifierconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.PatternMatcher;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;

class WifiDelegate {
    final private String ssid;
    final private String password;

    private final MethodChannel.Result result;

    final private Context context;

    final private WifiManager wifiManager;


    WifiDelegate(MethodCall call, MethodChannel.Result result, Context context) {
        Map data = (Map) call.arguments;
        this.ssid = (String) data.get("SSID");
        this.password = (String) data.get("PASSWORD");
        this.result = result;
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    void connect() {
        if (!wifiManager.isWifiEnabled()) {
            result.error("WIFI_TURNED_OFF", "Wifi Adapter is Offline", "Please Turn on the wifi adapter");
            return;
        }
        Log.e("SDK", ""+SDK_INT);
        if (SDK_INT >= VERSION_CODES.Q) {
            WifiNetworkSpecifier.Builder
                    builder = new WifiNetworkSpecifier.Builder()
                    .setSsidPattern(new PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
                    .setWpa2Passphrase(password);
            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

            NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                    .setNetworkSpecifier(wifiNetworkSpecifier.toString());
            networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

            NetworkRequest networkRequest = networkRequestBuilder.build();
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                cm.requestNetwork(networkRequest, new NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        Log.e("WIFIDelegate", "available");
                        super.onAvailable(network);
                    }

                    @Override
                    public void onUnavailable() {
                        Log.e("WIFIDelegate", "Not available");
                        super.onUnavailable();
                    }
                });
                result.success("Wifi Requested");
            } else {
                result.error("Connectivity_Manager_Not_Available", "Connectivity Manager is not available", "Connectivity Manager is not available");
            }
        } else {
            connectionBeforeQ();
        }


    }

    private void connectionBeforeQ() {
        WifiConfiguration wifiConfig = createWifiConfig();
        if (wifiConfig == null) {
            Log.e("WIFIDelegate", "wifi config is null");
            result.error("Wifi_Config_Is_Null", "Wifi Config is null", "Wifi Config is null");
            return;
        }
        int netId = wifiManager.addNetwork(wifiConfig);
        Log.e("WIFIDELEGATE", "Net ID: " + netId);
        if (SDK_INT < VERSION_CODES.O) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            result.success("wifi connected less than Oreo");
        } else {
            new NetworkChangeReceiver().connect(netId);
        }
    }


    private WifiConfiguration createWifiConfig() {
        WifiConfiguration prevConfig = isExist();
        if (prevConfig != null) {
            Log.e("createWifiConfig", "removing previous config!");
            Log.e("createWifiConfig", "ssid: " + prevConfig.SSID);
            Log.e("createWifiConfig", "networkId: " + prevConfig.networkId);
            if (wifiManager.removeNetwork(prevConfig.networkId)) {
                Log.e("createWifiConfig", "removed previous config!");
            } else {
                Log.e("createWifiConfig", "failed to remove previous config!");
                return null;
            }

        } else {
            Log.e("createWifiConfig", "no previous config!");
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"".concat(ssid).concat("\"");
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;

//        config.hiddenSSID = hidden;
//        switch (security) {
//            case "WEP":
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//                config.wepKeys[0] = "\"".concat(password).concat("\"");
//                config.wepTxKeyIndex = 0;
//                break;
//            case "WPA":
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        config.preSharedKey = "\"".concat(password).concat("\"");
//                break;
//            default:
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//                config.allowedAuthAlgorithms.clear();
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                break;
//        }
        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }

    private WifiConfiguration isExist() {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    class NetworkChangeReceiver extends BroadcastReceiver {
        private int netId;
        private boolean willLink = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("NetworkCR", intent.getAction());
            ConnectivityManager conn = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conn.getActiveNetworkInfo();
            if (networkInfo == null || (networkInfo.getState() == NetworkInfo.State.DISCONNECTED && willLink)) {
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                result.success("Wifi Connected greater than oreo");
                willLink = false;
                Log.e("NetworkCR", "wifi connected");
            }
        }

        public void connect(int netId) {
            this.netId = netId;
            willLink = true;
            wifiManager.disconnect();
        }
    }


}