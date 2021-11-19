package com.droid.auditory.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.droid.auditory.application.RxBus;
import com.droid.auditory.events.StartListeningEvent;
import com.droid.lib.utils.IntentUtils;

import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_PING;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_PING_RECEIVED;
import static com.droid.lib.constants.IntentConstants.ACTION_AUDITORY_START_LISTENING;

public class AuditoryBroadcastReceiver extends BroadcastReceiver {

    RxBus bus;
    IntentUtils intentUtils;

    public AuditoryBroadcastReceiver(RxBus bus, IntentUtils intentUtils) {
        this.bus = bus;
        this.intentUtils = intentUtils;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_AUDITORY_PING:
                final Intent auditoryPingIntent = intentUtils.createBroadcastIntent(ACTION_AUDITORY_PING_RECEIVED, new Bundle());
                intentUtils.sendBroadcast(context, auditoryPingIntent);
                break;
            case ACTION_AUDITORY_START_LISTENING:
                bus.post(new StartListeningEvent());
                break;
        }
    }

}
