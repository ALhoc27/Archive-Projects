package ru.dubrovinalexeyleonidovich.mobileagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.dubrovinalexeyleonidovich.mobileagent.services.LocationTrackingService;
import ru.dubrovinalexeyleonidovich.mobileagent.services.MainService;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        MainService.startService(context);

//        Intent actionIntent = new Intent("ru.dubrovinalexeyleonidovich.mobileagent.services.action.START");
//        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(actionIntent);

    }
}
