package com.example.kristin.rollout;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kristin.rollout.directionhelpers.JSONParser;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.RideParams;
import com.lyft.lyftbutton.RideTypeEnum;
import com.uber.sdk.android.core.UberButton;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.ServerTokenSession;

import com.lyft.networking.ApiConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.kristin.rollout.directionhelpers.FetchURL;
import com.example.kristin.rollout.directionhelpers.TaskLoadedCallback;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        TaskLoadedCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private FetchURL fetchURL;
    private JSONParser jsonParser;
    private LatLng latLng;
    double dropoff_lat;
    double dropoff_lng;
    double pickup_lat;
    double pickup_lng;
    double cabFare;
    String cabFare_string;
    Double maxLat = null, minLat = null, minLon = null, maxLon = null;
    MarkerOptions dropoff_marker, pickup_marker;
    Polyline currentPolyline;
    TextView dropoff_location;
    TextView pickup_location;
    TextView calculate_button;
    com.uber.sdk.android.rides.RideRequestButton uber_button;
    com.lyft.lyftbutton.LyftButton lyft_button;
    Button cab_button;
    TextView cab_fare;

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

    public void getInput() throws IOException {

        Geocoder coder = new Geocoder(this);

        // Geocodes Input into Address
        List<Address> dropoff_address;
        List<Address> pickup_address;

        // Fills in Current Location with User's Location
        pickup_lat = latLng.latitude;
        pickup_lng = latLng.longitude;
        pickup_address = coder.getFromLocation(pickup_lat, pickup_lng, 1);
        Address pickup_location = pickup_address.get(0);


        EditText pickup_location_input = findViewById(R.id.pickup_location);
        String pickup_location_text = pickup_location_input.getText().toString();

        // Gets User's Inputted Information
        EditText dropoff_location_input = findViewById(R.id.dropoff_location);
        String dropoff_location_text = dropoff_location_input.getText().toString();


        dropoff_address = coder.getFromLocationName(dropoff_location_text, 1);
        pickup_address = coder.getFromLocationName(pickup_location_text, 1);

        Address dropoff_location = dropoff_address.get(0);
        pickup_location = pickup_address.get(0);

        dropoff_lat = dropoff_location.getLatitude();
        dropoff_lng = dropoff_location.getLongitude();

        pickup_lat = pickup_location.getLatitude();
        pickup_lng = pickup_location.getLongitude();

        // Map Configurations
        pickup_marker = new MarkerOptions().position(new LatLng(pickup_lat, pickup_lng)).title("Pick Up");
        dropoff_marker = new MarkerOptions().position(new LatLng(dropoff_lat, dropoff_lng)).title("Drop Off");
        pickup_marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        mMap.addMarker(pickup_marker);
        mMap.addMarker(dropoff_marker);

        latLng = new LatLng(pickup_location.getLatitude(), pickup_location.getLongitude());

        String url = getUrl(pickup_marker.getPosition(), dropoff_marker.getPosition(), "driving");
        new FetchURL(this).execute(url, "driving");


        // Configuring Camera Bounds
        boolean hasPoints = false;
        Double maxLat = null, minLat = null, minLon = null, maxLon = null;
        if (currentPolyline != null && currentPolyline.getPoints() != null) {
            List<LatLng> pts = currentPolyline.getPoints();
            for (LatLng coordinate : pts) {

                maxLat = maxLat != null ? Math.max(coordinate.latitude, maxLat) : coordinate.latitude;
                minLat = minLat != null ? Math.min(coordinate.latitude, minLat) : coordinate.latitude;
                maxLon = maxLon != null ? Math.max(coordinate.longitude, maxLon) : coordinate.longitude;
                minLon = minLon != null ? Math.min(coordinate.longitude, minLon) : coordinate.longitude;
                hasPoints = true;
            }
        }

        if (hasPoints) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(maxLat, maxLon));
            builder.include(new LatLng(minLat, minLon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }

        createRide();
    }

    public void createRide() {

        lyftRide();
        uberRide();
        //cabRide();
    }

    public void uberRide() {
        // Uber Integration
        RideRequestButton requestButton = new RideRequestButton(this);
        UberButton uberButton = findViewById(R.id.uber_button);

        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(pickup_lat, pickup_lng, "", "")
                .setDropoffLocation(dropoff_lat, dropoff_lng, "", "")
                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                .build();

        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("-8NCdgaNOlzqc9mTkp4WMU4l5cm0wp2a")
                .setServerToken("ocXTg92LK-TjXCLC97lTJPly6WHyAbFbTdPnp1dV")
                .build();
        ServerTokenSession session = new ServerTokenSession(config);


        requestButton.setRideParameters(rideParams);
        requestButton.setSession(session);
        requestButton.loadRideInformation();
    }

    public void lyftRide() {
        // Lyft Integration
        ApiConfig apiConfig = new ApiConfig.Builder()
                .setClientId("93hozlE-5V07")
                .setClientToken("g7RBXeaGqQmVCk775iWW4ZQJA+I52Y46O5rYRa7b4GsMiqDnwjssYkydlycU4Fzs7CG3WnH+0K23DtCUxmeYHHWg9hgcvaJCWPd4TJov5DkPBXL2kO83Icw=")
                .build();

        LyftButton lyftButton = findViewById(R.id.lyft_button);
        lyftButton.setApiConfig(apiConfig);

        RideParams.Builder rideParamsBuilder = new RideParams.Builder()
                .setPickupLocation(pickup_lat, pickup_lng)
                .setDropoffLocation(dropoff_lat, dropoff_lng);
        rideParamsBuilder.setRideTypeEnum(RideTypeEnum.CLASSIC);

        lyftButton.setRideParams(rideParamsBuilder.build());
        lyftButton.load();
    }

    public void cabRide() {
        // Cab Integration

        JSONParser jsonParser = new JSONParser();

        String distance = jsonParser.distance;
        String duration = jsonParser.duration;

        distance = distance.replace(" mi", "");
        duration = duration.replace(" mins", "");

        float distance_float = Float.valueOf(distance);
        float duration_float = Float.valueOf(duration);

        cabFare = (3.75 + .25 * ((duration_float * 60) / 33) + 2.00 * (distance_float));

        cabFare = Math.round(cabFare * 100.0) / 100.0;

        cabFare_string = "$" + Double.toString(cabFare);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // 'Where To' Click
    public void textInputButton(View v) throws IOException {
        dropoff_location = findViewById(R.id.dropoff_location);
        pickup_location = findViewById(R.id.pickup_location);
        calculate_button = findViewById(R.id.price_calculate);
        dropoff_location.setY(250);
        dropoff_location.setTextAlignment(2);
        dropoff_location.setPadding(50, 0, 0, 0);
        pickup_location.setVisibility(View.VISIBLE);
        pickup_location.setPadding(50, 0, 0, 0);
        calculate_button.setVisibility(View.VISIBLE);


        Geocoder coder = new Geocoder(this);

        List<Address> pickup_address;

        pickup_lat = latLng.latitude;
        pickup_lng = latLng.longitude;

        pickup_address = coder.getFromLocation(pickup_lat, pickup_lng, 1);

        Address pickup_location_geocoder = pickup_address.get(0);

        pickup_location.setText(pickup_location_geocoder.getAddressLine(0));

    }

    // Calculate Price Click
    public void priceCalculateButton(View v) throws IOException {
        uber_button = findViewById(R.id.uber_button);
        lyft_button = findViewById(R.id.lyft_button);
        cab_button = findViewById(R.id.cab_button);
        cab_fare = findViewById(R.id.cab_fare);
        cab_fare.setText(cabFare_string);
        cab_fare.setVisibility(View.VISIBLE);
        cab_button.setVisibility(View.VISIBLE);
        uber_button.setVisibility(View.VISIBLE);
        lyft_button.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        getInput();
    }

    public void callCab(View v) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:123456789"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
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


    // Where Location is Being Updated
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

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline != null){
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions)values[0]);

    }

}
