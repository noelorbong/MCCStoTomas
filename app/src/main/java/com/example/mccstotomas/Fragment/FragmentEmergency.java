package com.example.mccstotomas.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import com.example.mccstotomas.BuildConfig;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.MainActivity;
import com.example.mccstotomas.Model.URLModel;
import com.example.mccstotomas.Model.UserModel;
import com.example.mccstotomas.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class FragmentEmergency extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    // location last updated time
    private String mLastUpdateTime;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    public Activity mainActivity;
    private GoogleMap mMap;
    private MapView mapView;
    private GoogleMap googleMap;
    private Fragment fragment;
    private double latitude =0;
    private  double longitude=0;
    private  Marker locMarker;
    private EditText et_note,et_quantity;
    private TextView emergency_title;
    Button  btn_confirm, btn_cancel;
    SqlLiteHelper db;
    private ProgressDialog progress;
    int id;
    private URLModel url = new URLModel();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view =  inflater.inflate(R.layout.frame_emergency,
                container, false);
        mainActivity = (MainActivity) getActivity();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        ButterKnife.bind(mainActivity);

        btn_confirm = (Button)view.findViewById(R.id.btn_confirm);
        btn_cancel  = (Button)view.findViewById(R.id.btn_cancel);
        et_note = (EditText)view.findViewById(R.id.et_note) ;
        et_quantity = (EditText)view.findViewById(R.id.et_quantity) ;
        emergency_title = (TextView) view.findViewById(R.id.emergency_title) ;
        // initialize the necessary libraries
        db = new SqlLiteHelper(mainActivity);
        db.open();
        final UserModel userModel = db.getSpecificUser();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            id = bundle.getInt("id", 0);
            String title = bundle.getString("title", "");
            String description = bundle.getString("description", "");
            et_quantity.setHint("Number of "+title+" required.");
            emergency_title.setText(title);

        }


        init();
        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
                if(isEditTextNotEmpty(et_quantity,"Quantity required.")){
                    if(latitude ==0 || longitude == 0){
                        Toast.makeText(mainActivity.getApplicationContext(), "Unable to get the current Location..", Toast.LENGTH_LONG).show();
                    }else{
                        RequestParams params = new RequestParams();
                        params.put("email", userModel.getEmail());
                        params.put("emergency_type", id);
                        params.put("quantity", et_quantity.getText().toString());
                        params.put("latitude",latitude);
                        params.put("longitude", longitude);
                        params.put("note", et_note.getText().toString());
                        letsDoSomeNetworking(url.Report,params);
                    }

                }

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameContact, new FragmentDashboard(), "NewFragmentTag");
                ft.commit();
            }
        });

        startLocationButtonClick();

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        return  view;
    }

    private boolean isEditTextNotEmpty(EditText editText,String response){
        boolean isNotNull;
        View focusView = null;
        if( TextUtils.isEmpty(editText.getText().toString().trim())){
            /**
             *   You can Toast a message here that the Username is Empty
             **/
            editText.setError( response);
            isNotNull = false;
            focusView = editText;
            focusView.requestFocus();
        }else{
            isNotNull = true;
        }
        return isNotNull;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(latitude == 0 || longitude == 0){
            Toast.makeText(mainActivity.getApplicationContext(), "Scanning current Location..", Toast.LENGTH_LONG).show();
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions a = new MarkerOptions().position(latLng).title("My Location");;
            locMarker = mMap.addMarker(a);
            locMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);
        }else{
            LatLng latLng = new LatLng(latitude, longitude);
            MarkerOptions a = new MarkerOptions().position(latLng).title("My Location");;
            locMarker = mMap.addMarker(a);
            locMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);
        }

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        mSettingsClient = LocationServices.getSettingsClient(mainActivity);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }


    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {


            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);
            locMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }



    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mainActivity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

//                        Toast.makeText(mainActivity.getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(mainActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(mainActivity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(mainActivity, errorMessage, Toast.LENGTH_LONG).show();
                        }

//                        updateLocationUI();
                    }
                });
    }

    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(mainActivity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    public void stopLocationButtonClick() {
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(mainActivity.getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();

                    }
                });
    }


    public void showLastKnownLocation() {
        if (mCurrentLocation != null) {
            Toast.makeText(mainActivity.getApplicationContext(), "Lat: " + mCurrentLocation.getLatitude()
                    + ", Lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mainActivity.getApplicationContext(), "Last known location is not available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // TODO: complete the letsDoSomeNetworking() method
    private void letsDoSomeNetworking(String url, RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();


        client.post(url,params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                //Start progress indicator here
                progress = ProgressDialog.show(mainActivity, "Sending Report..", "Please wait!!!");  //show a progress dialog
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("Response", "JSON: " + response.toString());

                try{
                    if(response.optString("response").equals("0")){

                        Toast.makeText(mainActivity, "Report Successful!", Toast.LENGTH_LONG).show();

                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment fragment = new FragmentDashboard();
                        ft.replace(R.id.frameContact, fragment, "NewFragmentTag");
                        ft.commit();
                    }else{
                        Toast.makeText(mainActivity, "Report Failed!", Toast.LENGTH_LONG).show();
                    }



                } catch (Exception e){
                    Log.e("Response", e.toString());
                    Toast.makeText(mainActivity, "Report Failed: !", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Response", "Request fail! Status code: " + statusCode);
                Log.d("Response", "Fail response: " + response);
                Log.e("ERROR", e.toString());
                Toast.makeText(mainActivity, "Report Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                progress.dismiss();
            }
        });


    }

}