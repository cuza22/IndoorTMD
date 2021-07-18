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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {
    IntentFilter mIntentFilter = new IntentFilter();
    WifiManager mWifiManager;

    public static final int INTERVAL = 10;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private Button startBtn;
    private Button endBtn;
    private TextView numberOfWifiTV;

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
    //**********************************************************************************//
    // Data list
    private static final String header = "Year, Month, Day, Hour, Min, Sec, ID, MAC, RSSI, FREQUENCY, CHANNEL WIDTH, Time";
    private File directory;
    private String fileName;
    private boolean isDataCollecting = false;

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

        // views
        mRecyclerView = findViewById(R.id.recyclerView);
        numberOfWifiTV = findViewById(R.id.numberOfWifiTV);
        startBtn = findViewById(R.id.startBtn);
        endBtn = findViewById(R.id.endBtn);

        startBtn.setEnabled(true);
        endBtn.setEnabled(false);

        // set wifi manager
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(mWifiScanReceiver, mIntentFilter);

        setDirectory();

    } // onCreate()

    // timer **********************************************************************************/
    private Timer timer;
    private TimerTask setTimerTask() {
        TimerTask dataCollect = new TimerTask() {
            @Override
            public void run() {
                mWifiManager.startScan();
            }
        };
        return dataCollect;
    }

    public void onClickStartBtn(View v) {
        v.setEnabled(false);
        endBtn.setEnabled(true);

        timer = new Timer();
        TimerTask dataCollect = setTimerTask();
        timer.schedule(dataCollect, 0, 1000 * INTERVAL);

        setFileName();
        isDataCollecting = true;
    }

    public void onClickEndBtn(View v) {
        v.setEnabled(false);
        startBtn.setEnabled(true);

        if (timer != null) {
            timer.cancel();
        }

        isDataCollecting = false;
    }

    // TODO 4: Toast 메시지 계속 뜨는 것 해결

    // WifiManager **********************************************************************************/
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

    // wifi 검색 성공
    private void scanSuccess() {
        // scanning success
        List<ScanResult> mScanResults = mWifiManager.getScanResults();

        // get number of wifi signals
        numberOfWifiTV.setText(String.valueOf(mScanResults.size()));

        // sort by rssi
        Comparator<ScanResult> comparator = (lhs, rhs) -> (lhs.level < rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(mScanResults, comparator.reversed());
        }

        // attach adapter to recyclerview
        mAdapter = new RecyclerViewAdapter(mScanResults, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // save as csv file
        writeInFile(mScanResults);

        // toast message
        Toast.makeText(getApplicationContext(), "scan success!", Toast.LENGTH_SHORT).show();

    } // scanSuccess()

    private void scanFailure() {
        Log.i("LOG","scan failed!");

        Toast.makeText(getApplicationContext(), "scan failed!", Toast.LENGTH_SHORT).show();

    } // scanFailure()

    // csv **********************************************************************************//
    private void setDirectory() {
        // set file directory
        //파일 저장 경로 설정
//        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirPath = getFilesDir().getAbsolutePath();
        //디렉토리 없으면 생성
        // TODO: directory is not being created
        directory = new File(dirPath);
        Log.i("FILE", String.valueOf(directory));
        if( !directory.exists() ) {
            Log.i("FILE", "directory doesn't exist");
            if (!directory.mkdirs()) {
                Log.i("FILE", "dir not created");
            }
        }

    }

    private void setFileName() {
        // create file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date time = new Date();
        fileName = dateFormat.format(time) + ".csv";
    }

    private void writeInFile(List<ScanResult> results) {
        // TODO: If a file doesn't exist, create a new one

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");

        try {
            CSVWriter cw = new CSVWriter(new FileWriter(directory + "/" + fileName, true));
            Iterator iter = results.iterator();
            try {
                while (iter.hasNext()) {
                    ScanResult result = (ScanResult) iter.next();
                    Date time = new Date();
                    String timeData = dateFormat.format(time);

                    String[] CSVString = { timeData, String.valueOf(result.SSID), String.valueOf(result.BSSID), String.valueOf(result.level),
                             String.valueOf(result.capabilities), String.valueOf(result.channelWidth), String.valueOf(result.timestamp) };

                    cw.writeNext(CSVString);
                }
            } finally {
                cw.close();
            }

        } catch (IOException e) {
            Log.e("FILE", "Can't Save");
            e.printStackTrace();
        }

    }

}
