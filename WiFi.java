package com.rasitech.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.util.List;

@DesignerComponent(
        version = 1,
        description = "WiFi Extension",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "")

@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.ACCESS_WIFI_STATE, android.permission.CHANGE_WIFI_STATE")

public class WiFiExtension extends AndroidNonvisibleComponent {

    private Context context;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;

    public WiFiExtension(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Register BroadcastReceiver for SCAN_RESULTS_AVAILABLE_ACTION
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    handleScanResults();
                }
            }
        };
        context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    // Event untuk menanggapi hasil pemindaian WiFi
    @SimpleEvent(description = "Fired when WiFi scan results are available.")
    public void GotWiFiScanResults(List<String> networks) {
        EventDispatcher.dispatchEvent(this, "GotWiFiScanResults", networks);
    }

    // Fungsi untuk memulai pemindaian WiFi
    @SimpleFunction(description = "Start WiFi scan.")
    public void StartWiFiScan() {
        wifiManager.startScan();
    }

    // Fungsi untuk mendapatkan informasi WiFi saat ini
    @SimpleFunction(description = "Get current WiFi information.")
    public void GetCurrentWiFiInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        String bssid = wifiInfo.getBSSID();
        int rssi = wifiInfo.getRssi();

        // Kirim informasi ke App Inventor
        GotCurrentWiFiInfo(ssid, bssid, rssi);
    }

    // Event untuk menyampaikan informasi WiFi saat ini ke App Inventor
    @SimpleEvent(description = "Fired when current WiFi information is available.")
    public void GotCurrentWiFiInfo(String ssid, String bssid, int rssi) {
        EventDispatcher.dispatchEvent(this, "GotCurrentWiFiInfo", ssid, bssid, rssi);
    }

    // Fungsi untuk menghubungkan ke jaringan WiFi tertentu
    @SimpleFunction(description = "Connect to a specific WiFi network.")
    public void ConnectToWiFiNetwork(String ssid, String password) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + password + "\"";
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        int networkId = wifiManager.addNetwork(wifiConfig);
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
    }

    // Fungsi untuk mematikan WiFi
    @SimpleFunction(description = "Turn off WiFi.")
    public void TurnOffWiFi() {
        wifiManager.setWifiEnabled(false);
    }

    // Fungsi untuk menyalakan WiFi
    @SimpleFunction(description = "Turn on WiFi.")
    public void TurnOnWiFi() {
        wifiManager.setWifiEnabled(true);
    }

    // Handle hasil pemindaian WiFi
    private void handleScanResults() {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        List<String> networks = extractNetworkNames(scanResults);
        GotWiFiScanResults(networks);
    }

    // Ekstrak nama jaringan dari hasil pemindaian
    private List<String> extractNetworkNames(List<ScanResult> scanResults) {
        // Anda dapat menyesuaikan informasi yang ingin Anda ambil dari ScanResult sesuai kebutuhan.
        // Contoh ini hanya mengambil nama SSID.
        List<String> networkNames = new java.util.ArrayList<>();
        for (ScanResult result : scanResults) {
            networkNames.add(result.SSID);
        }
        return networkNames;
    }
}
