package com.example.kristin.rollout;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;

import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.RideParams;
import com.lyft.lyftbutton.RideTypeEnum;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.error.ApiError;

import com.lyft.networking.ApiConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private LatLng latLng;
    double dropoff_lat;
    double dropoff_lng;
    double pickup_lat;
    double pickup_lng;
    TextView dropoff_location;
    TextView pickup_location;
    TextView calculate_button;
    com.uber.sdk.android.rides.RideRequestButton uber_button;
    com.lyft.lyftbutton.LyftButton lyft_button;

    private static final int Request_User_Location_Code = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    public void createRide() throws IOException {

        // Gets User Desired Location Information
        EditText dropoff_location_input = (EditText) findViewById(R.id.dropoff_location);
        String dropoff_location_text = dropoff_location_input.getText().toString();

        // Get Pickup Location
        EditText pickup_location_input = (EditText) findViewById(R.id.pickup_location);
        String pickup_location_text = dropoff_location_input.getText().toString();

        List<Address> dropoff_address;
        List<Address> pickup_address;

        Geocoder coder = new Geocoder(this);

        dropoff_address = coder.getFromLocationName(dropoff_location_text, 1);
        pickup_address = coder.getFromLocationName(pickup_location_text, 1);


        Address dropoff_location = dropoff_address.get(0);
        Address pickup_location = dropoff_address.get(0);

        dropoff_lat = dropoff_location.getLatitude();
        dropoff_lng = dropoff_location.getLongitude();

        pickup_lat = pickup_location.getLatitude();
        pickup_lng = pickup_location.getLongitude();


        // Lyft Integration
        ApiConfig apiConfig = new ApiConfig.Builder()
                .setClientId("93hozlE-5V07")
                .setClientToken("g7RBXeaGqQmVCk775iWW4ZQJA+I52Y46O5rYRa7b4GsMiqDnwjssYkydlycU4Fzs7CG3WnH+0K23DtCUxmeYHHWg9hgcvaJCWPd4TJov5DkPBXL2kO83Icw=")
                .build();

        LyftButton lyftButton = (LyftButton) findViewById(R.id.lyft_button);
        lyftButton.setApiConfig(apiConfig);

        RideParams.Builder rideParamsBuilder = new RideParams.Builder()
                .setPickupLocation(pickup_lat, pickup_lng)
                .setDropoffLocation(dropoff_lat, dropoff_lng);
        rideParamsBuilder.setRideTypeEnum(RideTypeEnum.CLASSIC);

        lyftButton.setRideParams(rideParamsBuilder.build());
        lyftButton.load();



        // Uber Integration
        RideRequestButton requestButton = new RideRequestButton(this);
        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(requestButton);

        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(37.775304, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
                .setDropoffLocation(41.20572, -81.415929, "Embarcadero", "2131 Weston Drive, Hudson Ohio 44236") // Price estimate will only be provided if this is provided.
                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d") // Optional. If not provided, the cheapest product will be used.
                .build();

        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("-8NCdgaNOlzqc9mTkp4WMU4l5cm0wp2a")
                .setServerToken("ocXTg92LK-TjXCLC97lTJPly6WHyAbFbTdPnp1dV")
                //.setRedirectUri("<REDIRECT_URI>")
                .build();

        ServerTokenSession session = new ServerTokenSession(config);

        RideRequestButtonCallback callback = new RideRequestButtonCallback() {

            @Override
            public void onRideInformationLoaded() {

            }

            @Override
            public void onError(ApiError apiError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        };

        requestButton.setRideParameters(rideParams);
        requestButton.setSession(session);
        requestButton.setCallback(callback);
        requestButton.loadRideInformation();
}


    // Where To Click
    public void textInputButton(View v) {
        dropoff_location = findViewById(R.id.dropoff_location);
        pickup_location = findViewById(R.id.pickup_location);
        calculate_button = findViewById(R.id.price_calculate);
        dropoff_location.setY(250);
        dropoff_location.setTextAlignment(2);
        dropoff_location.setPadding(50,0,0,0);
        pickup_location.setVisibility(1);
        pickup_location.setPadding(50,0,0,0);
        calculate_button.setVisibility(1);

    }

    // Calculate Price Click
    public void priceCalculateButton(View v) throws IOException {
        uber_button = findViewById(R.id.uber_button);
        lyft_button = findViewById(R.id.lyft_button);
        uber_button.setVisibility(1);
        lyft_button.setVisibility(1);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        createRide();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            buildGoogleApiClient();

            mMap.setMyLocationEnabled(true);

        }
    }

    public boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }

            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }

            return false;
        }

        else
        {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_User_Location_Code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT);
                }

                return;
        }

    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        if(currentUserLocationMarker != null)
        {
            currentUserLocationMarker.remove();
        }

        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("User Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        currentUserLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(14));

        if(googleApiClient != null)
        {

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);

        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
