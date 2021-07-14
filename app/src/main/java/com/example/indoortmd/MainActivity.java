package com.example.indoortmd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Context context = getApplicationContext();
    IntentFilter mIntentFilter = new IntentFilter();
//    WifiManager mWifiManager = null;
    WifiManager mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter mAdapter = null;

    // WifiManager
    BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        // wifiManager.startScan()시 발동되는 메소드
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive");
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if(success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        } // onReceive()
    };

//    IntentFilter mIntentFilter = new IntentFilter();
//    mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//    context.registerReceiver(mWifiScanReceiver, mIntentFilter);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // main view loading
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // recycler view
        mRecyclerView = findViewById(R.id.recyclerView);

        // permissions

        // wifi manager
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(mWifiScanReceiver, mIntentFilter);

    } // onCreate()

    public void onClick(View view) {
        System.out.println("onClick");
        boolean success = mWifiManager.startScan();
        System.out.println(success);
        if(!success) {
            scanFailure();
        }
    } // onClick()

    // wifi 검색 성공
    private void scanSuccess() {
        System.out.println("scan success!"); // debug
        List<ScanResult> mScanResults = mWifiManager.getScanResults();
        System.out.println("scan result size"); // debug
        System.out.println(mScanResults.size()); // debug
        ArrayList<RecyclerViewItem> mScanResultsData = scanResultsToRecyclerViewItems(mScanResults);

        mAdapter = new RecyclerViewAdapter(mScanResultsData, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void scanFailure() {
        System.out.println("scan failed!");
    } // scanSuccess()

    private ArrayList<RecyclerViewItem> scanResultsToRecyclerViewItems(List<ScanResult> results) {
        ArrayList<RecyclerViewItem> resultsData = new ArrayList<>();

        Iterator iterator = results.listIterator();
        while(iterator.hasNext()) {
            // set temp RVI
            RecyclerViewItem newRecyclerViewItem = new RecyclerViewItem();
            // get wifi data
            ScanResult wifiData = (ScanResult) iterator.next();
            String id = wifiData.SSID; // wifi 이름
            String mac = wifiData.BSSID; // mac 주소
            int rssi = wifiData.level; // rssi
            // put wifi data in temp RVI
            newRecyclerViewItem.setId(id);
            newRecyclerViewItem.setMac(mac);
            newRecyclerViewItem.setRssi(rssi);
            // append temp RVI to RVI array
            resultsData.add(newRecyclerViewItem);
        }

        return resultsData;
    }

//    @Override
//    public void onDenied(int i, String[] strings) {
//        Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
//    }
//    @Override
//    public void onGranted(int i, String[] strings) {
//        Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
//    }

//    public class ViewHolder extends RecyclerView.ViewHolder {
//        TextView ipText;
//        TextView macText;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            ipText = itemView.findViewById(R.id.ip);
//            macText = itemView.findViewById(R.id.mac);
//        }
//    }
}
