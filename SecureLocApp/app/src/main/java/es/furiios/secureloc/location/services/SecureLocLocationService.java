package es.furiios.secureloc.location.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import es.furiios.secureloc.log.Logger;
import es.furiios.secureloc.notifications.NotificationHandler;

/**
 * Main app service. It scans for wifi-provided locations, and compares them to trusty GPS.
 */
public class SecureLocLocationService extends Service implements LocationListener {

    private static final String TAG = "LocatorLocationService";
    private static Intent service;
    private static boolean inited;

    private LocationManager mLocationManager;
    private SharedPreferences mPreferences;
    private Location mLastNetworkLocation;
    private boolean isRequesting;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        inited = true;
        Logger.v(TAG, "LocatorLocationService started!");
        mPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        if (mPreferences.getBoolean("service", true)) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);
            }
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                mLastNetworkLocation = location;
                NotificationHandler.getInstance(this).sendUsingNetworkNotification();
                if (!isRequesting && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    isRequesting = true;
                    mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                }
            }
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                NotificationHandler.getInstance(this).sendUsingGpsNotification();
                if (mLastNetworkLocation != null) {
                    if (mLastNetworkLocation.distanceTo(location) > (mLastNetworkLocation.getAccuracy() + location.getAccuracy()) * 1.32) {
                        NotificationHandler.getInstance(this).sendWarningNotification(mLastNetworkLocation, location);
                    }
                    isRequesting = false;
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        inited = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
            NotificationHandler.getInstance(this).removeAllNotifications();
        }
        Logger.v(TAG, "Stopping SecureLocLocationService...");
    }

    public static void init(Activity activity) {
        if (!inited) {
            service = new Intent(activity, SecureLocLocationService.class);
            activity.startService(service);
        }
    }

    public static void finish(Activity activity) {
        if (inited) {
            activity.stopService(service);
        }
    }

    public static boolean isInited() {
        return inited;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
