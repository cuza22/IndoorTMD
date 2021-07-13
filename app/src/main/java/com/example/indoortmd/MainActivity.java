package com.example.indoortmd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    IntentFilter mIntentFilter = new IntentFilter();
    WifiManager mWifiManager = null;

    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter mAdapter = null;
    ArrayList<RecyclerViewItem> mList;

//    private String mIdText;
//    private String mMacText;
    // WifiManager
    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        // wifiManager.startScan()시 발동되는 메소드
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if(success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        } // onReceive()
    };

    // TODO 1: scanSuccess()
    // wifi 검색 성공
    private void scanSuccess() {
        List<ScanResult> mScanResults = mWifiManager.getScanResults();
        ArrayList<RecyclerViewItem> mScanResultsData = scanResultsToRecyclerViewItems(mScanResults);

        mAdapter = new RecyclerViewAdapter(mScanResultsData);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void scanFailure() {
    } // scanSuccess()

    private ArrayList<RecyclerViewItem> scanResultsToRecyclerViewItems(List<ScanResult> results) {
        ArrayList<RecyclerViewItem> resultsData = new ArrayList<>();

        // TODO 2: use iterator
        Iterator iterator = results.listIterator();
        while(iterator.hasNext()) {
            // set temp RVI
            RecyclerViewItem newRecyclerViewItem = new RecyclerViewItem();
            // get wifi data
            ScanResult wifiData = (ScanResult) iterator.next();
            String id = wifiData.SSID.toString(); // wifi 이름
            String mac = wifiData.BSSID.toString(); // mac 주소
            // put wifi data in temp RVI
            newRecyclerViewItem.setId(id);
            newRecyclerViewItem.setMac(mac);
            // append temp RVI to RVI array
            resultsData.add(newRecyclerViewItem);
        }

        return resultsData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // main view loading
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // recycler view
        mRecyclerView = findViewById(R.id.recyclerView);
        mList = new ArrayList<>();

        // wifi manager
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, mIntentFilter);

    } // onCreate()

    public void onClick(View view) {
        mWifiManager.startScan();
    } // onClick()

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView ipText;
        TextView macText;

        ViewHolder(View itemView) {
            super(itemView);
            ipText = itemView.findViewById(R.id.ip);
            macText = itemView.findViewById(R.id.mac);
        }
    }
}