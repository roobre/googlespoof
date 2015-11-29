package es.furiios.secureloc.location.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.furiios.secureloc.location.services.SecureLocLocationService;
import es.furiios.secureloc.log.Logger;

public class WakeUpSecureLoc extends BroadcastReceiver {

    private static final String TAG = "WakeUpSecureLoc";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.v(TAG, "BOOT COMPLETED received. Starting SecureLocLocationService...");
        Intent i = new Intent(context, SecureLocLocationService.class);
        context.startService(i);
    }
}
