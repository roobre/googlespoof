package es.furiios.secureloc.generics.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import es.furiios.secureloc.R;


/**
 * Clase de apoyo para el mapa. Esta clase contiene las funciones básicas para una activity que quiere
 * añadir y quitar marcadores, hacer zoom en uno de ellos u encuadrar el mapa en todos ellos,
 */
public abstract class MapActivity extends AppCompatActivity {

    protected GoogleMap mGoogleMap;
    protected SupportMapFragment mMapFragment;
    protected HashMap<String, Marker> mMapMarkers;

    private String currentTag;
    private boolean autoCenter;

    protected abstract void onInited();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapMarkers = new HashMap<>();

        onInited();
    }

    protected void removeMarker(String tag) {
        if (mMapMarkers.containsKey(tag)) {
            mMapMarkers.get(tag).remove();
            mMapMarkers.remove(tag);
        }
    }

    protected void addMarker(String tag, MarkerOptions m) {
        mMapMarkers.put(tag, mGoogleMap.addMarker(m));
    }

    protected void centerOnMarker(String tag) {
        if (mMapMarkers.containsKey(tag)) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(mMapMarkers.get(tag).getPosition()).zoom(18).bearing(0).build()));
        }
    }

    protected void centerOnMarkers() {
        if (!mMapMarkers.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker m : mMapMarkers.values()) {
                builder.include(m.getPosition());
            }

            final LatLngBounds centeredBounds = builder.build();
            mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    if (autoCenter && mMapMarkers.size() > 1) {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centeredBounds, (int) (100 * Resources.getSystem().getDisplayMetrics().density)));
                    } else if (mMapMarkers.size() == 1) {
                        centerOnMarker(mMapMarkers.keySet().iterator().next());
                    } else if (!autoCenter && currentTag != null) {
                        centerOnMarker(currentTag);
                    }
                }
            });
        }
    }

    protected void setGoogleMap(GoogleMap map) {
        mGoogleMap = map;
    }

    protected void enableAutoCenter() {
        autoCenter = true;
    }

    protected void disableAutoCenter() {
        autoCenter = false;
    }

    protected void setCurrentTag(String tag) {
        currentTag = tag;
    }
}
