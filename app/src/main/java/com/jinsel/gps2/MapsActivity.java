package com.jinsel.gps2;

//***************************************************************************
//GPS2: Updated GPS App with Google Maps API
//Joshua Insel
// --------------------------------------------------------------------------
// Date     Notes
// ======== =================================================================
// 12/3/15  App started, basic map created
// 12/5/15  My Location button and Location Services API client implemented
// 12/6/15  Geocoder, click events, text views, and buttons implemented
//***************************************************************************

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener, LocationListener {

    private GoogleMap mMap; //Map
    private GoogleApiClient mGoogleApiClient; //Api client
    public Location mCurrentLocation; //Current location
    private LatLng mCurrentLatLng; //Current latitude and longitude
    LocationRequest mLocationRequest; //Request for location updates
    final int MY_PERMISSIONS_REQUEST_LOCATION = 1; //Code for permission request
    private AddressResultReceiver mResultReceiver;
    private Marker mMarker; //Used whenever a marker is created
    private Marker mSelectedMarker; //The marker a user has clicked on

    public TextView speed; //Current moving speed
    public TextView altitude; //Current altitude
    public TextView accuracy; //Accuracy of GPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        buildGoogleApiClient();
        createLocationRequest();
        speed = (TextView) findViewById(R.id.speedText);
        altitude = (TextView) findViewById(R.id.altitudeText);
        accuracy = (TextView) findViewById(R.id.accuracyText);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    protected synchronized void buildGoogleApiClient() { //Builds API client for Location Services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //Sets map to satellite imagery and road layouts
        mMap.setOnMapClickListener(this); //Sets click listener on map
        mMap.setOnMapLongClickListener(this); //Sets long click listener on map
        mMap.setOnMarkerClickListener(this); //Sets marker click listener
        mMap.setOnMyLocationButtonClickListener(this); //Sets click listener for My Location button

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) // Checks for location permission
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, //Requests location permission
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true); //Turns on My Location button
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //Result of location permission request
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                return;
            }
        }
    }

    protected void createLocationRequest() { //Creates request for location updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) { //When Google API client is connected
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMarker = mMap.addMarker(new MarkerOptions().position(mCurrentLatLng).title(location(mCurrentLatLng))); //Creates marker at current location with coordinates
        startIntentService(mCurrentLatLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, 15));
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) { //When the map is pressed for a long time
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location(latLng))); //Creates marker at pressed location with coordinates
        startIntentService(latLng);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        speed.setText(roundUp(mCurrentLocation.getSpeed(), 2) + " m/s");
        altitude.setText(roundUp(mCurrentLocation.getAltitude(), 2) + " meters");
        accuracy.setText(roundUp(mCurrentLocation.getAccuracy(), 2) + " meters");
    }

    public String location(LatLng mLatLng){ //Converts decimal coordinates to traditional coordinates
        String text = new Latitude(mLatLng.latitude).toString() + ", " + new Longitude(mLatLng.longitude).toString();
        return text;
    }


    private void addAddress(String address) { //Adds address data to marker
        mMarker.setSnippet(address);
    }

    protected void startIntentService(LatLng mLatLng) { //Takes latitude and longitude and searches for address data in intent service
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLatLng);
        startService(intent);
    }

    @Override
    public boolean onMyLocationButtonClick() { //When My Location button is clicked
        mMarker = mMap.addMarker(new MarkerOptions().position(mCurrentLatLng).title(location(mCurrentLatLng))); //Creates marker at current location
        startIntentService(mCurrentLatLng);
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) { //When marker is clicked
        mSelectedMarker = marker;
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) { //When map is pressed for a short time
        mSelectedMarker = null; //Marker not selected
    }

    class AddressResultReceiver extends ResultReceiver { //Receives address data for location from intent service
        public String mAddressOutput;
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) { //When result is received from intent service
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) { //If address is successfully found
                addAddress(mAddressOutput);
            }
        }
    }

    public static double roundUp(double value, int places) { //Rounds large decimals to specified number of places
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public void deleteOnClick(View view) { //When delete button is clicked
        if (mSelectedMarker != null) { //If a marker is selected
            mSelectedMarker.remove(); //Removes selected marker from map
        }
        else {
            Toast toast = Toast.makeText(this, "No marker selected", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    public void deleteAllOnClick(View view) { //When delete all button is clicked
        mMap.clear(); //Clears all markers from map
    }
}
