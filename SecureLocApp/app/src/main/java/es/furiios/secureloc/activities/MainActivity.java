package es.furiios.secureloc.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import es.furiios.secureloc.R;
import es.furiios.secureloc.generics.activities.MapActivity;
import es.furiios.secureloc.location.services.SecureLocLocationService;

public class MainActivity extends MapActivity implements OnMapReadyCallback, LocationListener {

    private LocationManager mLocationManager;
    private Location lastNetworkLocation, lastGpsLocation;
    private FloatingActionButton zoomCenter, zoomWifi, zoomGps;

    @Override
    protected void onInited() {
        mMapFragment.getMapAsync(this);
        SecureLocLocationService.init(this);

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
}
