package es.furiios.secureloc;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<Marker> mMapMarkers;
    private LocationManager mLocationManager;
    private boolean NETWORK_ENABLED, GPS_ENABLED;
    private FloatingActionButton zoomWifi, zoomGps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMapMarkers = new ArrayList<>();
        zoomWifi = (FloatingActionButton) findViewById(R.id.zoom_wifi);
        zoomGps = (FloatingActionButton) findViewById(R.id.zoom_gps);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        NETWORK_ENABLED = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        GPS_ENABLED = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        final Location networkLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        final Location gpsLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location pasiveLastLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        if (networkLastLocation != null) {
            final LatLng network = new LatLng(networkLastLocation.getLatitude(), networkLastLocation.getLongitude());
            mMapMarkers.add(googleMap.addMarker(new MarkerOptions().position(network).title("Network").icon(BitmapDescriptorFactory.fromResource(networkLastLocation.isFromMockProvider() ? R.mipmap.ic_wifi_fake : R.mipmap.ic_wifi_legit))));
            zoomWifi.setImageResource(networkLastLocation.isFromMockProvider() ? R.mipmap.ic_wifi_fake : R.mipmap.ic_wifi_legit);

            builder.include(network);
            zoomWifi.setVisibility(View.VISIBLE);
            zoomWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(network).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            });
        }

        if (gpsLastLocation != null) {
            final LatLng gps = new LatLng(gpsLastLocation.getLatitude(), gpsLastLocation.getLongitude());
            mMapMarkers.add(googleMap.addMarker(new MarkerOptions().position(gps).title("Gps").icon(BitmapDescriptorFactory.fromResource(gpsLastLocation.isFromMockProvider() ? R.mipmap.ic_gps_fake : R.mipmap.ic_gps_legit))));
            zoomGps.setImageResource(gpsLastLocation.isFromMockProvider() ? R.mipmap.ic_gps_fake : R.mipmap.ic_gps_legit);

            builder.include(gps);
            zoomGps.setVisibility(View.VISIBLE);
            zoomGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            });
        }

        /*if (pasiveLastLocation != null) {
            LatLng pasive = new LatLng(pasiveLastLocation.getLatitude(), pasiveLastLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(pasive).title("Pasive (" + pasiveLastLocation.getProvider() + ")").icon(BitmapDescriptorFactory.fromResource(pasiveLastLocation.isFromMockProvider() ? (pasiveLastLocation.getProvider().equals(LocationManager.GPS_PROVIDER.toLowerCase()) ? R.mipmap.ic_gps_fake : R.mipmap.ic_wifi_fake) : (pasiveLastLocation.getProvider().equals(LocationManager.GPS_PROVIDER.toLowerCase()) ? R.mipmap.ic_gps_legit : R.mipmap.ic_wifi_legit))));
        }*/

        final LatLngBounds centeredBounds = builder.build();
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mMapMarkers.size() == 2) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centeredBounds, (int) (100 * Resources.getSystem().getDisplayMetrics().density)));
                } else if (mMapMarkers.size() == 1) {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(mMapMarkers.get(0).getPosition()).zoom(18).bearing(0)/*.tilt(30)*/.build()));
                }
            }
        });
    }
}
