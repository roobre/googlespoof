package es.furiios.secureloc.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import es.furiios.secureloc.R;
import es.furiios.secureloc.generics.activities.MapActivity;
import es.furiios.secureloc.location.services.SecureLocLocationService;

public class MainActivity extends MapActivity implements OnMapReadyCallback, LocationListener {

    private TextView logNetwork, logGps;
    private SharedPreferences mPreferences;
    private LocationManager mLocationManager;
    private Location lastNetworkLocation, lastGpsLocation;
    private FloatingActionButton zoomCenter, zoomWifi, zoomGps;

    @Override
    protected void onInited() {
        mPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);

        mMapFragment.getMapAsync(this);
        if (mPreferences.getBoolean("debug", true)) {
            SecureLocLocationService.init(this);
        }

        logNetwork = (TextView) findViewById(R.id.log_network);
        logGps = (TextView) findViewById(R.id.log_gps);

        if (mPreferences.getBoolean("debug", false)) {
            logNetwork.setVisibility(View.VISIBLE);
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        zoomCenter = (FloatingActionButton) findViewById(R.id.zoom_center);
        zoomWifi = (FloatingActionButton) findViewById(R.id.zoom_wifi);
        zoomGps = (FloatingActionButton) findViewById(R.id.zoom_gps);

        zoomCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentTag(null);
                enableAutoCenter();
                centerOnMarkers();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lastGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        setGoogleMap(googleMap);
        addNetworkMarker(lastNetworkLocation);
        addGpsMarker(lastGpsLocation);
        enableAutoCenter();
        centerOnMarkers();
    }

    private void addNetworkMarker(Location loc) {
        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            addMarker(LocationManager.NETWORK_PROVIDER, new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource((loc.isFromMockProvider() ? R.mipmap.ic_wifi_fake : R.mipmap.ic_wifi_legit))));
            setFABImageResource(zoomWifi, loc.isFromMockProvider() ? R.mipmap.ic_wifi_fake : R.mipmap.ic_wifi_legit);
            setFABVisibility(zoomCenter, View.VISIBLE);
            setFABVisibility(zoomWifi, View.VISIBLE);

            logNetwork.setText("Network->Lat: " + loc.getLatitude() + "\tLng: " + loc.getLongitude() + "\tAcc: " + loc.getAccuracy());

            zoomWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disableAutoCenter();
                    setCurrentTag(LocationManager.NETWORK_PROVIDER);
                    centerOnMarker(LocationManager.NETWORK_PROVIDER);
                }
            });
        }
    }

    private void addGpsMarker(Location loc) {
        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            addMarker(LocationManager.GPS_PROVIDER, new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource((loc.isFromMockProvider() ? R.mipmap.ic_gps_fake : R.mipmap.ic_gps_legit))));
            setFABImageResource(zoomGps, loc.isFromMockProvider() ? R.mipmap.ic_gps_fake : R.mipmap.ic_gps_legit);
            setFABVisibility(zoomCenter, View.VISIBLE);
            setFABVisibility(zoomGps, View.VISIBLE);

            logGps.setText("Gps->Lat: " + loc.getLatitude() + "\tLng: " + loc.getLongitude() + "\tAcc: " + loc.getAccuracy());

            zoomGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disableAutoCenter();
                    setCurrentTag(LocationManager.GPS_PROVIDER);
                    centerOnMarker(LocationManager.GPS_PROVIDER);
                }
            });
        }
    }

    private void setFABImageResource(final FloatingActionButton fab, final int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setImageResource(res);
            }
        });
    }

    private void setFABVisibility(final FloatingActionButton fab, final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setVisibility(visibility);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            removeMarker(location.getProvider());
            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                addNetworkMarker(location);
                centerOnMarkers();
            } else if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                addGpsMarker(location);
                //Logger.v("es.furiios.secureloc", "Acc: " + location.getAccuracy());
                centerOnMarkers();
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
        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            setFABVisibility(zoomWifi, View.GONE);
        } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
            setFABVisibility(zoomGps, View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setChecked(mPreferences.getBoolean("debug", false));
        menu.getItem(1).setChecked(mPreferences.getBoolean("service", true));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_debug:
                if (item.isChecked()) {
                    item.setChecked(false);
                    logGps.setVisibility(View.GONE);
                    logNetwork.setVisibility(View.GONE);
                    mPreferences.edit().putBoolean("debug", false).commit();
                } else {
                    item.setChecked(true);
                    logGps.setVisibility(View.VISIBLE);
                    logNetwork.setVisibility(View.VISIBLE);
                    mPreferences.edit().putBoolean("debug", true).commit();
                }
                return true;
            case R.id.action_service:
                if (item.isChecked()) {
                    item.setChecked(false);
                    mPreferences.edit().putBoolean("service", false).commit();
                    SecureLocLocationService.finish(this);
                } else {
                    item.setChecked(true);
                    mPreferences.edit().putBoolean("service", true).commit();
                    SecureLocLocationService.init(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
