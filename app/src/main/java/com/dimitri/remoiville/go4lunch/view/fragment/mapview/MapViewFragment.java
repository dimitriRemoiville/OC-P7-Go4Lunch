package com.dimitri.remoiville.go4lunch.view.fragment.mapview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dimitri.remoiville.go4lunch.BuildConfig;
import com.dimitri.remoiville.go4lunch.R;
import com.dimitri.remoiville.go4lunch.event.AutocompleteEvent;
import com.dimitri.remoiville.go4lunch.model.User;
import com.dimitri.remoiville.go4lunch.view.activity.DetailsPlaceActivity;
import com.dimitri.remoiville.go4lunch.viewmodel.Injection;
import com.dimitri.remoiville.go4lunch.viewmodel.MainViewModel;
import com.dimitri.remoiville.go4lunch.viewmodel.SingletonCurrentUser;
import com.dimitri.remoiville.go4lunch.viewmodel.ViewModelFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MapViewFragment extends Fragment
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private MainViewModel mMainViewModel;
    private GoogleMap mMap;
    private final String API_KEY = BuildConfig.API_KEY;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrentLocation;
    private final int mRadius = 400;
    private Context mContext;
    private User mCurrentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        configureViewModel();
        View root = inflater.inflate(R.layout.fragment_mapview, container, false);
        mContext = root.getContext();

        mCurrentUser = SingletonCurrentUser.getInstance().getCurrentUser();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        // Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        // Async Map
        supportMapFragment.getMapAsync(this);

        return root;
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory();
        mMainViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel.class);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mCurrentLocation = location;
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
                    mMap.moveCamera(update);
                    loadRestaurantsFromPlaces();
                } else {
                    Toast.makeText(mContext, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadRestaurantsFromPlaces() {
        // get users with a place ID != null
        mMainViewModel.getUsersPlaceIDNotNull().observe(this, users -> {
            // load restaurants nearby
            mMainViewModel.getListRestaurants().observe(getViewLifecycleOwner(), places -> {
                // looking for places with workmates
                for (int i = 0; i < places.size(); i++) {
                    for (int j = 0; j < users.size(); j++) {
                        if (places.get(i).getPlaceId().equals(users.get(j).getRestaurantID())) {
                            places.get(i).getUserList().add(users.get(j));
                        }
                    }
                }

                // Adding markers on the map
                for (int i = 0; i < places.size(); i++) {
                    LatLng position = new LatLng(places.get(i).getLat(), places.get(i).getLng());
                    MarkerOptions markerOptions = new MarkerOptions().position(position)
                            .title(places.get(i).getName())
                            .alpha(0.9f);

                    if (mCurrentUser.getRestaurantID() != null && mCurrentUser.getRestaurantID().equals(places.get(i).getPlaceId())) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    } else {
                        if (places.get(i).getUserList().size() > 0) {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        } else {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        }
                    }
                    mMap.addMarker(markerOptions).setTag(places.get(i).getPlaceId());
                }
            });
            configureClickOnMarker();
        });
    }

    private void configureClickOnMarker() {
        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(mContext, DetailsPlaceActivity.class);
            intent.putExtra("placeId", marker.getTag().toString());
            startActivity(intent);
            return false;
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    if (location.getLatitude() != mCurrentLocation.getLatitude()
                            && location.getLongitude() != mCurrentLocation.getLongitude()) {
                        mMap.clear();
                        mCurrentLocation = location;
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
                        mMap.moveCamera(update);
                        mMainViewModel.clearListRestaurants();
                        mMainViewModel.setListRestaurants(mCurrentLocation, mRadius, API_KEY);
                    }
                } else {
                    Toast.makeText(mContext, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAutocompletePlace(AutocompleteEvent event) {
        if (event.place.getLatLng() != null && event.place.getName() != null && mMap != null) {
            Place place = event.place;
            LatLng position = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            mMap.addMarker(new MarkerOptions().position(position)
                    .title(place.getName())
                    .alpha(0.9f)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    .setTag(place.getId());

            LatLng currentLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            mMap.animateCamera(update);
        }
    }
}
