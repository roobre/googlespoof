package es.furiios.secureloc;

import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public abstract class MapActivity extends AppCompatActivity {

    protected GoogleMap mGoogleMap;
    protected HashMap<String, Marker> mMapMarkers;

    private boolean autoCenter;
    private String currentMarker;
    private FloatingActionButton zoomCenter, zoomWifi, zoomGps;

    protected abstract void onInited();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMapMarkers = new HashMap<>();
        zoomCenter = (FloatingActionButton) findViewById(R.id.zoom_center);
        zoomWifi = (FloatingActionButton) findViewById(R.id.zoom_wifi);
        zoomGps = (FloatingActionButton) findViewById(R.id.zoom_gps);

        zoomCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAutoCenter();
                currentMarker = "Both";
                centerOnMarkers();
            }
        });

        onInited();
    }

    protected void addMarker(Location loc, String tag) {
        final LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMapMarkers.put(tag, mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(tag.toUpperCase()).icon(BitmapDescriptorFactory.fromResource(loc.isFromMockProvider() ? ((tag.equals(LocationManager.NETWORK_PROVIDER)) ? R.mipmap.ic_wifi_fake : R.mipmap.ic_gps_fake) : ((tag.equals(LocationManager.NETWORK_PROVIDER)) ? R.mipmap.ic_wifi_legit : R.mipmap.ic_gps_legit)))));
        if (tag.equals(LocationManager.NETWORK_PROVIDER)) {
            zoomWifi.setImageResource(loc.isFromMockProvider() ? R.mipmap.ic_wifi_fake : R.mipmap.ic_wifi_legit);
            zoomWifi.setVisibility(View.VISIBLE);
            zoomWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableAutoCenter();
                    currentMarker = LocationManager.NETWORK_PROVIDER;
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            });
        } else if (tag.equals(LocationManager.GPS_PROVIDER)) {
            zoomGps.setImageResource(loc.isFromMockProvider() ? R.mipmap.ic_gps_fake : R.mipmap.ic_gps_legit);
            zoomGps.setVisibility(View.VISIBLE);
            zoomGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableAutoCenter();
                    currentMarker = LocationManager.GPS_PROVIDER;
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            });
        }
    }

    protected void centerOnMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : mMapMarkers.values()) {
            builder.include(m.getPosition());
        }

        final LatLngBounds centeredBounds = builder.build();
        mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mMapMarkers.size() == 2 && isAutoCenterEnabled()) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centeredBounds, (int) (100 * Resources.getSystem().getDisplayMetrics().density)));
                } else if (mMapMarkers.size() == 1 || !isAutoCenterEnabled()) {
                    if (currentMarker != null && !currentMarker.equals("Both"))
                        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(mMapMarkers.get(currentMarker).getPosition()).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            }
        });
    }

    protected void enableAutoCenter() {
        autoCenter = true;
    }

    protected void disableAutoCenter() {
        autoCenter = false;
    }

    private boolean isAutoCenterEnabled() {
        return autoCenter;
    }
}
