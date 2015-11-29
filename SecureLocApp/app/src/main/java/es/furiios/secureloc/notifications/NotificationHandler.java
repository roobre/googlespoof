package es.furiios.secureloc.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

import es.furiios.secureloc.R;

public class NotificationHandler {

    private static NotificationHandler instance = null;

    private Context mContext;
    private NotificationManager mNotificationManager;

    public static NotificationHandler getInstance(Context context) {
        if (NotificationHandler.instance == null) {
            NotificationHandler.instance = new NotificationHandler();

            NotificationHandler.instance.setContext(context);
            NotificationHandler.instance.setNotificationManager();
        }
        return NotificationHandler.instance;
    }

    private void setContext(Context context) {
        this.mContext = context;
    }

    private void setNotificationManager() {
        this.mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void sendWarningNotification(Location network, Location gps) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.you_probably_are_victim_of_a_location_falsification))
                .setAutoCancel(true);
        mNotificationManager.notify(0, builder.build());
    }
}
