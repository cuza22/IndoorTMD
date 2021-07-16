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
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    IntentFilter mIntentFilter = new IntentFilter();
    WifiManager mWifiManager;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;

    final String CoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    final String AccessWifi = Manifest.permission.ACCESS_WIFI_STATE;
    final String ChangeWifi = Manifest.permission.CHANGE_WIFI_STATE;

    //**********************************************************************************//
    // Permission request codes
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
    private static final int ACCESS_COARSE_LOCATION_REQUEST_CODE = 102;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 104;
    private static final int LOCATION_REQUEST_CODE = 111;
    private static final int STORAGE_REQUEST_CODE = 112;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSION_IGNORE_OPTIMIZATION_REQUEST_CODE = 200;

    // GPS, storage, wifi **********************************************************************************//
    @Override
    protected void onStart() {
        super.onStart();

        // check permissions when API over 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE
                }, PERMISSION_REQUEST_CODE);
            }
        }

        // check permissions
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(CoarseLocation) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
//            }
//            if (checkSelfPermission(AccessWifi) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, 123);
//            }
//            if (checkSelfPermission(ChangeWifi) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, 123);
//            }
//        }
    }
    // network **********************************************************************************//
    @Override
    protected void onResume() {
        super.onResume();

        // check network availability ( over API 28 )
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

    }
    //**********************************************************************************//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // recycler view
        mRecyclerView = findViewById(R.id.recyclerView);

        // set wifi manager
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(mWifiScanReceiver, mIntentFilter);

    } // onCreate()
    //**********************************************************************************//

    // WifiManager
    BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        // wifiManager.startScan()시 발동되는 메소드
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("TAG","onReceive");
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if(success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        } // onReceive()
    };

//    public void onClick(View v)
//    {
//        Log.i("TAG","onClick"); // debug
//
//        // start scanning
//        mWifiManager.startScan();
//
//    } // onClick()

    // wifi 검색 성공
    private void scanSuccess() {
        // scanning success
        List<ScanResult> mScanResults = mWifiManager.getScanResults();
        Log.i("EX",mScanResults.get(0).capabilities);

        // sort by rssi
        Comparator<ScanResult> comparator = (lhs, rhs) -> (lhs.level < rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(mScanResults, comparator.reversed());
        }

        // attach adapter to recyclerview
        mAdapter = new RecyclerViewAdapter(mScanResults, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // toast message
        Toast.makeText(getApplicationContext(), "scan success!", Toast.LENGTH_SHORT).show();

    } // scanSuccess()

    private void scanFailure() {
        Log.i("LOG","scan failed!");

        Toast.makeText(getApplicationContext(), "scan failed!", Toast.LENGTH_SHORT).show();

    } // scanFailure()

}
