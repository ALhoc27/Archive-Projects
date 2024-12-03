package ru.dubrovinalexeyleonidovich.mobileagent.services;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import ru.dubrovinalexeyleonidovich.mobileagent.AppNotification;
import ru.dubrovinalexeyleonidovich.mobileagent.Constants;
import ru.dubrovinalexeyleonidovich.mobileagent.GPSCoordinates;
import ru.dubrovinalexeyleonidovich.mobileagent.MyApplication;

import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_MIN_DISTANCE;
import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_MIN_TIME;


public class LocationTrackingService extends IntentService {
    private static final String ACTION_START_TRACK_LOCATION = "ru.dubrovinalexeyleonidovich.mobileagent.action.START_TRACK_LOCATION";

    private static final String EXTRA_MIN_TIME = "ru.dubrovinalexeyleonidovich.mobileagent.extra.EXTRA_MIN_TIME";
    private static final String EXTRA_MIN_DISTANCE = "ru.dubrovinalexeyleonidovich.mobileagent.extra.EXTRA_MIN_DISTANCE";

    LocationManager locationManager;
    LocationListener locationListener;

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };

    public LocationTrackingService() {
        super("LocationTrackingService");
    }

    static void startTrackLocation(Context context, float minTime, float minDistance) {
        if (isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction(ACTION_START_TRACK_LOCATION);
        intent.putExtra(EXTRA_MIN_TIME, minTime);
        intent.putExtra(EXTRA_MIN_DISTANCE, minDistance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationTrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_TRACK_LOCATION.equals(action)) {
                final float minTime = intent.getFloatExtra(EXTRA_MIN_TIME, DEFAULT_MIN_TIME);
                final float minDistance = intent.getFloatExtra(EXTRA_MIN_DISTANCE, DEFAULT_MIN_DISTANCE);
                handleStartTrackLocation(minTime, minDistance);
            }
        }
    }

    private void handleStartTrackLocation(float minTime, float minDistance) {
        long minTimeLong = (long) minTime * 1000;

        AppNotification.createNotification(this);

        locationListener = getLocationListener();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Нет прав для отслеживания",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

                stopSelf();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minTimeLong,
                    minDistance,
                    locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    minTimeLong,
                    minDistance,
                    locationListener);
        } else {
            stopSelf();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("STOP_LocationTrackingService"));

    }

    private LocationListener getLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String userId = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.USER_ID, "");

                if ("".equals(userId)) {
                    stopSelf();
                    return;
                }
                saveLocation(location, userId);

//                //////////
//                final Location locationFinal = location;
//                Handler mHandler = new Handler(getMainLooper());
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Координаты сохранены-" + locationFinal.toString(),
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });
//                //////////
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                String userId = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.USER_ID, "");

                if ("".equals(userId)) {
                    stopSelf();
                    return;
                }

                LocationManager locationManager = (LocationManager) MyApplication.applicationContext.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!enabled) {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settingsIntent);

                        //////////
                        Handler mHandler = new Handler(getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Для работы приложения включите GPS",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                        //////////
                    }
                }
            }
        };
    }

    private void saveLocation(Location location, String userId) {
        if (location == null) {
            return;
        }

        GPSCoordinates gps = new GPSCoordinates();
        gps.setUserId(userId);
        gps.setLongitude(location.getLongitude());
        gps.setLatitude(location.getLatitude());
        gps.setAccuracy(location.getAccuracy());
        gps.setDateStamp(location.getTime());
        gps.save();
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
