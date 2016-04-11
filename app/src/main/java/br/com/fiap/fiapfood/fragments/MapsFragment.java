package br.com.fiap.fiapfood.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import br.com.fiap.fiapfood.MainActivity;
import br.com.fiap.fiapfood.R;
import br.com.fiap.fiapfood.adapters.CustomWindowAdapter;
import br.com.fiap.fiapfood.models.Restaurant;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements IFilterableFragment {

    private static String TAG = "MapsFragment";
    @Bind(R.id.mapView) MapView mMapView;

    private GoogleMap googleMap;
    private HashMap<String, Restaurant> markers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity activity = (MainActivity) getActivity();
        activity.activeFragment = this;
        activity.setTitle(R.string.map_mode);

        activity.fab.setVisibility(View.VISIBLE);
        activity.navigationView.getMenu().findItem(R.id.maps_restaurants).setChecked(true);

        View view = inflater.inflate(R.layout.fragment_maps, container,
                false);

        ButterKnife.bind(this, view);

        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.clear();

        markers  = new HashMap<String, Restaurant>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < activity.restaurants.size(); i++) {
            Restaurant restaurant = activity.restaurants.get(i);
            addMarker(restaurant);

            markers.put(restaurant.name, restaurant);
            builder.include(new LatLng(restaurant.latitude, restaurant.longitude));
        }

        if(markers.size() == 0) {
            Toast.makeText(activity, R.string.no_restaurants_found, Toast.LENGTH_LONG).show();
        }else{
            final LatLngBounds bounds = builder.build();

            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 30);
                    googleMap.animateCamera(camera);
                }
            });
        }

        googleMap.setInfoWindowAdapter(new CustomWindowAdapter(getContext(), markers));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                final Restaurant restaurant = markers.get(marker.getTitle());
                marker.remove();

                addMarker(restaurant);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle(restaurant.name);

                builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity activity = (MainActivity) getContext();
                        FormFragment fragment = new FormFragment();

                        Bundle args=new Bundle();
                        args.putLong(FormFragment.BUNDLE_RESTAURANT_TO_EDIT_KEY, restaurant.getId());

                        fragment.setArguments(args);
                        activity.changeFragment(fragment);
                    }
                });

                builder.setPositiveButton(R.string.get_me_there, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=" + restaurant.latitude + "," + restaurant.longitude));
                        startActivity(intent);
                    }
                });

                android.app.AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }
        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        googleMap.setMyLocationEnabled(true);

        return view;
    }

    private void addMarker(Restaurant restaurant){
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(restaurant.latitude, restaurant.longitude))
                .title(restaurant.name);

        marker.icon(BitmapDescriptorFactory
                .fromResource(R.mipmap.ic_pin));

        googleMap.addMarker(marker).setDraggable(true);
    }

    public void onFilterResult(){
        MainActivity activity = (MainActivity) getActivity();
        googleMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < activity.restaurants.size(); i++) {
            Restaurant restaurant = activity.restaurants.get(i);
            addMarker(restaurant);
            builder.include(new LatLng(restaurant.latitude, restaurant.longitude));
        }

        if(activity.restaurants.size() == 0){
            Toast.makeText(getContext(), R.string.no_restaurants_found,
                    Toast.LENGTH_SHORT).show();
        }else{
            LatLngBounds bounds = builder.build();
            CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 30);
            googleMap.animateCamera(camera);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);

        if(mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null)
            mMapView.onLowMemory();
    }
}
