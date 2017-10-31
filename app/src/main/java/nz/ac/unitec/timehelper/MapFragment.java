package nz.ac.unitec.timehelper;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by eugene on 29/10/2017.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback/*, ClassLocationAvailableListener*/ {

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    List<ClassItem> mClassList;

//    @Override
    public void onClassLocationAvailable(final List<ClassItem> classes, MainActivity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ClassItem classItem : classes) {
                    double lat = classItem.getLat();
                    double lng = classItem.getLng();
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(classItem.getStart() + " (" + classItem.getVenue() + ")").snippet(classItem.getTitle()));
                    CameraPosition camPos = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).tilt(45).build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                }

                mView.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_START_CAMERA_APP && resultCode == Activity.RESULT_OK) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        Switch swMapType = (Switch) mView.findViewById(R.id.swMapType);
        swMapType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else {
                    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView)view.findViewById(R.id.map);
        if(mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MapsInitializer.initialize(getContext());
        }

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setPadding(0, 0, 0, 140);
    }
}
