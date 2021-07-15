package com.example.indoortmd;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
//    Context context;
    IntentFilter mIntentFilter = new IntentFilter();
    WifiManager mWifiManager;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    final String CoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    final String AccessWifi = Manifest.permission.ACCESS_WIFI_STATE;
    final String ChangeWifi = Manifest.permission.CHANGE_WIFI_STATE;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // main view loading
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        context = getApplicationContext();

        // recycler view
        mRecyclerView = findViewById(R.id.recyclerView);

        // set wifi manager
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);

//        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        getApplicationContext().registerReceiver(mWifiScanReceiver, mIntentFilter);

    } // onCreate()

    public void onClick(View v)
    {
        System.out.println("onClick"); // debug

        // check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CoarseLocation) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }

            if (checkSelfPermission(AccessWifi) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 123);
            }

            if (checkSelfPermission(ChangeWifi) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 123);
            }
        }

        // check for network ( over API 23 )
        LocationManager lman = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = false;

        try
        {
            network_enabled = lman.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        if (!network_enabled)
        {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }

        // start scanning
        mWifiManager.startScan();

    } // onClick()

    // wifi 검색 성공
    private void scanSuccess() {
//        System.out.println("scan success!"); // debug
        List<ScanResult> mScanResults = mWifiManager.getScanResults();
//        System.out.println("scan result size"); // debug
//        System.out.println(mScanResults.size()); // debug
//        System.out.println(mScanResults); // debug

        mAdapter = new RecyclerViewAdapter(mScanResults, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);
//        System.out.println(mRecyclerView.getLayoutManager());
        mAdapter.notifyDataSetChanged();

        Toast.makeText(getApplicationContext(), "scan success!", Toast.LENGTH_SHORT).show();

    } // scanSuccess()

    private void scanFailure() {
        System.out.println("scan failed!");

        Toast.makeText(getApplicationContext(), "scan failed!", Toast.LENGTH_SHORT).show();

    } // scanFailure()

}
