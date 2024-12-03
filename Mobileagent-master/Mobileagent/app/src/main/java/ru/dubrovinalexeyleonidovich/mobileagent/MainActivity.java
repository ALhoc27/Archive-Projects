package ru.dubrovinalexeyleonidovich.mobileagent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Pattern;

import ru.dubrovinalexeyleonidovich.mobileagent.services.MainService;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_START = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.START";
    private static final String ACTION_STOP = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.STOP";
    private static final String ACTION_STATUS = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.STATUS";
    private static final String ACTION_ID = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.ID";
    private static final String ACTION_INSTALL_APK = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.ACTION_INSTALL_APK";

    private static final String MIN_TIME = "MinTime";
    private static final String MIN_DISTANCE = "MinDistance";
    private static final String UNLOADING_TIME = "UnloadingTime";

    private static final String BASE_NAME = "BaseName";
    private static final String SERVER1 = "Server1";
    private static final String SERVER2 = "Server2";
    private static final String USER_LOGIN = "UserLogin";
    private static final String USER_PASSWORD = "UserPassword";
    private static final String USER_ID = "UserID";
    private static final String APK_PATH = "ApkPath";


    private static final int PERMISSION_REQUEST_CODE = 1;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);

        } else {
            actionOfIntent();
        }

//        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    actionOfIntent();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    private void actionOfIntent() {
        if (intent == null) return;
        final String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case ACTION_START:
                String userLogin = intent.getStringExtra(USER_LOGIN);
                String userPassword = intent.getStringExtra(USER_PASSWORD);
                if ("".equals(userLogin)) return;
                if (userLogin != null && userPassword != null) {
                    SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE);
                    sPref.edit().putString(Constants.USER_LOGIN, userLogin).apply();
                    sPref.edit().putString(Constants.USER_PASSWORD, userPassword).apply();
                }
//                MyApplication.initRetrofit(userLogin, userPassword);

                String userId = intent.getStringExtra(USER_ID);
                String server1 = intent.getStringExtra(SERVER1);
                String server2 = intent.getStringExtra(SERVER2);
                String baseName = intent.getStringExtra(BASE_NAME);

                String minTimeString = intent.getStringExtra(MIN_TIME);
                String minDistanceString = intent.getStringExtra(MIN_DISTANCE);
                String unloadingTimeString = intent.getStringExtra(UNLOADING_TIME);

                float minTime = Constants.DEFAULT_MIN_TIME;
                if (minTimeString != null) {
                    minTime = Float.parseFloat(minTimeString);
                }

                float minDistance = Constants.DEFAULT_MIN_DISTANCE;
                if (minDistanceString != null) {
                    minDistance = Float.parseFloat(minDistanceString);
                }

                float unloadingTime = Constants.DEFAULT_UNLOADING_TIME;
                if (unloadingTimeString != null) {
                    unloadingTime = Float.parseFloat(unloadingTimeString);
                }

                if (userId != null && server1 != null && server2 != null && baseName != null) {
                    MainService.startService(getApplicationContext(), userId, server1, server2, baseName, minTime, minDistance, unloadingTime);
                } else {
                    MainService.startService(getApplicationContext());
                }
                break;
            case ACTION_STOP:
                MainService.stopService(getApplicationContext());
                break;
            case ACTION_STATUS:
                Intent intentResultStatus = MainService.statusService(getApplicationContext());
                int status = intentResultStatus.getIntExtra("Status", 0);
                setResult(status, intentResultStatus);
                break;
            case ACTION_ID:
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (telephonyManager == null) return;
                String deviceIMEI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceIMEI = telephonyManager.getImei();
                } else {
                    deviceIMEI = telephonyManager.getDeviceId();
                }
                if (deviceIMEI == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceIMEI = telephonyManager.getMeid();
                }
                Intent intentResultIMEI = new Intent();
                intentResultIMEI.putExtra("IMEI", String.valueOf(deviceIMEI));
                intentResultIMEI.putExtra("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intentResultIMEI.putExtra("BASE_OS", String.valueOf(Build.VERSION.BASE_OS));
                }
                intentResultIMEI.putExtra("RELEASE", String.valueOf(Build.VERSION.RELEASE));
                intentResultIMEI.putExtra("CODENAME", String.valueOf(Build.VERSION.CODENAME));
                intentResultIMEI.putExtra("MANUFACTURER", String.valueOf(Build.MANUFACTURER));
                intentResultIMEI.putExtra("BRAND", String.valueOf(Build.BRAND));
                intentResultIMEI.putExtra("MODEL", String.valueOf(Build.MODEL));
                intentResultIMEI.putExtra("PRODUCT", String.valueOf(Build.PRODUCT));
                intentResultIMEI.putExtra("DEVICE", String.valueOf(Build.DEVICE));
                intentResultIMEI.putExtra("CPU_ABI", String.valueOf(Build.CPU_ABI));
                intentResultIMEI.putExtra("DISPLAY", String.valueOf(Build.DISPLAY));
                intentResultIMEI.putExtra("FINGERPRINT", String.valueOf(Build.FINGERPRINT));
                intentResultIMEI.putExtra("ID", String.valueOf(Build.ID));
                intentResultIMEI.putExtra("BOARD", String.valueOf(Build.BOARD));
                intentResultIMEI.putExtra("TYPE", String.valueOf(Build.TYPE));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intentResultIMEI.putExtra("SERIAL", String.valueOf(Build.getSerial()));
                }
                setResult(200, intentResultIMEI);
                break;
            case ACTION_INSTALL_APK:
                break;
        }
        finish();
    }



}
