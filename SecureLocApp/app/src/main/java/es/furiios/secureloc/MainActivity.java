package es.furiios.secureloc;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MainActivity extends MapActivity implements OnMapReadyCallback {

    private LocationManager mLocationManager;
    private NotificationManager mNotificationManager;

    @Override
    protected void onInited() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        if (mMapMarkers.containsKey(location.getProvider())) {
                            mMapMarkers.get(location.getProvider()).remove();
                            addMarker(location, location.getProvider());
                            centerOnMarkers();
                        }

                        int res, color;
                        String description;
                        if (location.isFromMockProvider()) {
                            color = Color.RED;
                            res = (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) ? R.mipmap.ic_wifi_legit : R.mipmap.ic_gps_legit;
                            description = (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) ? "Geolocation by Network Mock!" : "Geolocation by GPS Mock!";
                        } else {
                            color = Color.BLUE;
                            res = (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) ? R.mipmap.ic_wifi_legit : R.mipmap.ic_gps_legit;
                            description = (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) ? "Geolocation by Network seems legit!" : "Geolocation by GPS seems legit!";
                        }

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
                        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                .setSmallIcon(res)
                                .setColor(color)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(description)
                                .setOngoing(true)
                                .setAutoCancel(false);
                        mNotificationManager.notify(0, builder.build());
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
            });
        }
    }

    @Override
    public void onMapReady(final GoogleMap gMap) {
        mGoogleMap = gMap;
        enableAutoCenter();
        mGoogleMap.setMapType(4);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Location networkLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            final Location gpsLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location pasiveLastLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (networkLastLocation != null) {
                addMarker(networkLastLocation, LocationManager.NETWORK_PROVIDER);
            }

            if (gpsLastLocation != null) {
                addMarker(gpsLastLocation, LocationManager.GPS_PROVIDER);
            }
        }

        centerOnMarkers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNotificationManager.cancelAll();
    }
}
