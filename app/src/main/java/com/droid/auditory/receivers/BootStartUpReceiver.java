package com.droid.auditory.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droid.auditory.services.AuditoryService;
import com.droid.lib.constants.IntentConstants;

public class BootStartUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start Service On Boot Start Up
        Log.d("SERVICE", "onStart");

        Intent service = new Intent(context, AuditoryService.class);
        service.setAction(IntentConstants.ACTION_AUDITORY_STARTUP);
        context.startService(service);

    }

}
