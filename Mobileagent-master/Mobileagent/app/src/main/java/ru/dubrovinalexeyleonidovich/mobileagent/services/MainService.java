package ru.dubrovinalexeyleonidovich.mobileagent.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import ru.dubrovinalexeyleonidovich.mobileagent.BuildConfig;
import ru.dubrovinalexeyleonidovich.mobileagent.Constants;
import ru.dubrovinalexeyleonidovich.mobileagent.MyApplication;

import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_MIN_DISTANCE;
import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_MIN_TIME;
import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_UNLOADING_TIME;

public class MainService extends IntentService {
    private static final String ACTION_START = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.START";
    private static final String ACTION_STOP = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.STOP";
    private static final String ACTION_STATUS = "ru.dubrovinalexeyleonidovich.mobileagent.services.action.STATUS";

    private static final String MIN_TIME = "MinTime";
    private static final String MIN_DISTANCE = "MinDistance";
    private static final String USER_ID = "UserID";

    private static final String SERVER1 = "Server1";
    private static final String SERVER2 = "Server2";
    private static final String BASE_NAME = "BaseName";
    private static final String UNLOADING_TIME = "UnloadingTime";

    public MainService() {
        super("MainService");
    }


    public static void startService(Context context, String userId, String server1, String server2, String baseName, float minTime, float minDistance, float unloadingTime) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_START);

        intent.putExtra(USER_ID, userId);
        intent.putExtra(SERVER1, server1);
        intent.putExtra(SERVER2, server2);
        intent.putExtra(BASE_NAME, baseName);

        intent.putExtra(MIN_TIME, minTime);
        intent.putExtra(MIN_DISTANCE, minDistance);
        intent.putExtra(UNLOADING_TIME, unloadingTime);

        context.startService(intent);
    }

    public static void startService(Context context) {
//        SharedPreferences sPref = context.getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE);
//        String userLogin = sPref.getString(Constants.USER_LOGIN,"");
//        String userPassword = sPref.getString(Constants.USER_PASSWORD,"");
//        MyApplication.initRetrofit(userLogin, userPassword);

        Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    public static Intent statusService(Context context) {
        int status = 200;
        String message = "";

        String userId = context.getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE).getString(Constants.USER_ID, "");
        if (userId.equals("")) {
            status = 406;
            message = "Пользователь не авторизован";
        }

        if (!UnloadingCoordinatesService.isServiceRunning(context)) {
            status = 405;
            message = "Сервис не запущен";
        }

        if (!LocationTrackingService.isServiceRunning(context)) {
            status = 404;
            message = "Сервис не запущен";
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {
                status = 403;
                message = "Геопозиционирование не включено";
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            status = 402;
            message = "Нет прав для геопозиционирования";
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            status = 401;
            message = "Нет прав для получения информации об устройстве";
        }

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        Intent intent = new Intent();
        intent.putExtra("Status", status);
        intent.putExtra("Message", message);
        intent.putExtra("VersionCode", versionCode);
        intent.putExtra("VersionName", versionName);

        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {

                final String userId = intent.getStringExtra(USER_ID);
                final String server1 = intent.getStringExtra(SERVER1);
                final String server2 = intent.getStringExtra(SERVER2);
                final String baseName = intent.getStringExtra(BASE_NAME);

                final float minTime = intent.getFloatExtra(MIN_TIME, DEFAULT_MIN_TIME);
                final float minDistance = intent.getFloatExtra(MIN_DISTANCE, DEFAULT_MIN_DISTANCE);
                final float unloadingTime = intent.getFloatExtra(UNLOADING_TIME, DEFAULT_UNLOADING_TIME);

                handleStart(userId, server1, server2, baseName, minTime, minDistance, unloadingTime);

            } else if (ACTION_STOP.equals(action)) {
                handleStop();
            }
        }
    }

    private void handleStart(String userId, String server1, String server2, String baseName, float minTime, float minDistance, float unloadingTime) {
        SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE);

        if (userId != null && !"".equals(userId)) {
            sPref.edit().putString(Constants.USER_ID, userId).apply();
        }

        if (server1 != null && !"".equals(server1)) {
            sPref.edit().putString(Constants.SERVER1, server1).apply();
        }

        if (server2 != null && !"".equals(server2)) {
            sPref.edit().putString(Constants.SERVER2, server2).apply();
        }

        if (baseName != null && !"".equals(baseName)) {
            sPref.edit().putString(Constants.BASE_NAME, baseName).apply();
        }

        LocationTrackingService.startTrackLocation(getApplicationContext(), minTime, minDistance);
        UnloadingCoordinatesService.startUnloadingCoordinates(getApplicationContext(), unloadingTime);
    }

    private void handleStop() {
        SharedPreferences sPref = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE);
        sPref.edit().putString(Constants.USER_ID, "").apply();

        Intent intent1 = new Intent("STOP_LocationTrackingService");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        Intent intent2 = new Intent("STOP_UnloadingCoordinatesService");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
    }
}
