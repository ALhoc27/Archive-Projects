package ru.dubrovinalexeyleonidovich.mobileagent.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import ru.dubrovinalexeyleonidovich.mobileagent.AppNotification;
import ru.dubrovinalexeyleonidovich.mobileagent.Constants;
import ru.dubrovinalexeyleonidovich.mobileagent.GPSCoordinates;
import ru.dubrovinalexeyleonidovich.mobileagent.MyApplication;
import ru.dubrovinalexeyleonidovich.mobileagent.RequestsApi;

import static ru.dubrovinalexeyleonidovich.mobileagent.Constants.DEFAULT_UNLOADING_TIME;

public class UnloadingCoordinatesService extends IntentService {
    private static final String START_UNLOADING_COORDINATES = "ru.dubrovinalexeyleonidovich.mobileagent.action.START_UNLOADING_COORDINATES";

    private static final String EXTRA_UNLOADING_TIME = "ru.dubrovinalexeyleonidovich.mobileagent.services.extra.EXTRA_UNLOADING_TIME";

    private CountDownTimer timerLoad;

    String server;
    String server1;
    String server2;
    String userLogin;
    String userPassword;
    String baseName;

    int sendAttemptCounter = 0;

    public UnloadingCoordinatesService() {
        super("UnloadingCoordinatesService");
    }

    static void startUnloadingCoordinates(Context context, float unloadingTime) {
        if (isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, UnloadingCoordinatesService.class);
        intent.setAction(START_UNLOADING_COORDINATES);
        intent.putExtra(EXTRA_UNLOADING_TIME, unloadingTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (UnloadingCoordinatesService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (START_UNLOADING_COORDINATES.equals(action)) {

                String server1 = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.SERVER1, "");
                String server2 = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.SERVER2, "");
                String userLogin = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.USER_LOGIN, "");
                String userPassword = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.USER_PASSWORD, "");
                String baseName = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                        .getString(Constants.BASE_NAME, "");
                final float unloadingTime = intent.getFloatExtra(EXTRA_UNLOADING_TIME, DEFAULT_UNLOADING_TIME);
                handleStartUnloadingCoordinates(server1, server2, baseName, unloadingTime, userLogin, userPassword);
            }
        }
    }

    private void handleStartUnloadingCoordinates(String server1, String server2, String baseName, float unloadingTime, String userLogin, String userPassword) {
        long unloadingTimeLong = (long) unloadingTime * 1000;
        this.server1 = server = server1;
        this.server2 = server2;
        this.userLogin = userLogin;
        this.userPassword = userPassword;
        this.baseName = baseName;

        AppNotification.createNotification(this);

        if (timerLoad == null) {
            timerLoad = new CountDownTimer(unloadingTimeLong, unloadingTimeLong) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    unloadCoordinates();
                    start();
                }
            }.start();
            unloadCoordinates();
        }
    }

    private void unloadCoordinates() {
        sendAttemptCounter = sendAttemptCounter + 1;

        List<GPSCoordinates> gpsList = SQLite.select().from(GPSCoordinates.class).queryList();

        String userId = getSharedPreferences(Constants.APP_PREFERENCES, MODE_PRIVATE)
                .getString(Constants.USER_ID, "");

        if (userId.equals("") && (gpsList.isEmpty() || sendAttemptCounter > 5)) {
            stopSelf();
            return;
        }

        if (gpsList.isEmpty()) {
            return;
        }

        try {

//            String URL = RequestsApi.unloadGPSCoordinatesURL;
//            URL = URL.replace("{server}", server);
//            URL = URL.replace("{baseName}", baseName);
            final Response response = MyApplication.getRetrofit(server, userLogin, userPassword).unloadGPSCoordinates(
                    baseName,
                    gpsList
            ).execute();

//            //////////
//            final String responseCode = String.valueOf(response.code());
//            final Handler mHandler = new Handler(getMainLooper());
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),
//                            "Код от сервера: " + responseCode,
//                            Toast.LENGTH_LONG).show();
//                }
//            });
//            //////////

            if (response.code() == 200) {
                sendAttemptCounter = 0;
                for (GPSCoordinates coordinate : gpsList) {
                    coordinate.delete();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            changeServer();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            changeServer();
        }

    }

    private void changeServer() {

        if (server.equals(server1)) {
            server = server2;
        } else {
            server = server1;
        }

    }

    @Override
    public void onDestroy() {
        if (timerLoad != null) {
            timerLoad.cancel();
            timerLoad = null;
        }
        super.onDestroy();
    }
}
