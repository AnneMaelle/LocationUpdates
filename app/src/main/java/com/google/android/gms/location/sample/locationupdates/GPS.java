package com.google.android.gms.location.sample.locationupdates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult.Callback;
import com.google.maps.model.*;
import com.google.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static java.lang.Math.abs;


/**
 * Using location settings.
 * <p/>
 * Uses the {@link com.google.android.gms.location.SettingsApi} to ensure that the device's system
 * settings are properly configured for the app's location needs. When making a request to
 * Location services, the device's system settings may be in a state that prevents the app from
 * obtaining the location data that it needs. For example, GPS or Wi-Fi scanning may be switched
 * off. The {@code SettingsApi} makes it possible to determine if a device's system settings are
 * adequate for the location request, and to optionally invoke a dialog that allows the user to
 * enable the necessary settings.
 * <p/>
 * This sample allows the user to request location updates using the ACCESS_FINE_LOCATION setting
 * (as specified in AndroidManifest.xml). The sample requires that the device has location enabled
 * and set to the "High accuracy" mode. If location is not enabled, or if the location mode does
 * not permit high accuracy determination of location, the activity uses the {@code SettingsApi}
 * to invoke a dialog without requiring the developer to understand which settings are needed for
 * different Location requirements.
 */
public class GPS extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        TextToSpeech.OnInitListener,
        OnMapReadyCallback {

    protected static final String TAG = "GPS";
                
    //Constante utilisée dans les paramètres de dialogue
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
                
    //Intervalle entre chaque mise à jour de Location, il ne faut pas que la valeur soit trop basse   
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
                
    //Taux de mise à jour de position le plus rapide. Les mises à jour ne seront jamais plus fréquentes
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Clés pour stocker des activités dans le bundle
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    protected String language = "fr";

    //Point d'entrée pour les services Google Play
    protected GoogleApiClient mGoogleApiClient;

    //Stocke les paramètres pour les requêtes à l'API FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;
    
    //Stocke  les types des services de postition par lesquelles le client est intéressé. Est utilisé pour vérifier si l'appareil a les paramètres optimaux.
    protected LocationSettingsRequest mLocationSettingsRequest;

    //Représente une position géographique
    protected Location mCurrentLocation;
    protected Location oldLocation = new Location("me");
    protected LatLng originLoc;
                
    //Vitesse
    protected double currentSpeed = 0.0;
    protected double oldSpeed = 0.0;
                
    protected double currentTime = 0.0;
    double EARTH_RADIUS = 6367.45;

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLocationInadequateWarning;
    protected TextView mSpeedTextView;
    protected GoogleMap myMap;
    private TextToSpeech tts;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;
    protected String mSpeedLabel;

    //            
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
                
    protected String mLastUpdateTime;

    // Itinéraire
    private String origin = "Metz";
    private String destination = "Paris";

    //Vrai si l'origine est précisée par l'utilisateur, faux si il demande MaPosition
    boolean originGiven = true;

    //Construction de l'itinéraire
    DirectionsRoute[] myRoutes;
    DirectionsLeg[] myLegs;
    DirectionsStep[] mySteps;
    Vector<com.google.android.gms.maps.model.LatLng> debutStep;
    int indiceCurrentStep;
    EncodedPolyline[] myPolylines;
    String[] instructions;
    Vector<com.google.android.gms.maps.model.LatLng> trajetPredit = new Vector<>();
    Troncon t;

    boolean consigne100 = true;

    int indiceDernierePos = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Crée le TextToSpeech
        tts = new TextToSpeech(this, this);

        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mLocationInadequateWarning = (TextView) findViewById(R.id.location_inadequate_warning);
        mSpeedTextView = (TextView) findViewById(R.id.speed_text);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        mSpeedLabel = "Speed";

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        // Récupération des points de départ et arrivée pour l'itinéraire.
        Intent myIntent = getIntent();
        destination = myIntent.getStringExtra("Destination");
        originGiven = myIntent.getBooleanExtra("Origine donnée",true);

        if(originGiven){
            origin = myIntent.getStringExtra("Origine");
            //originLoc = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());

            //Crée la requête d'itinéraire
            GeoApiContext context = new GeoApiContext().setApiKey(getResources().getString(R.string.google_maps_directions));
            DirectionsApiRequest request = DirectionsApi.newRequest(context).origin(origin).destination(destination).language(language
            );
            // Envoie la requête de manière Asynchrone et stocke les résultats
            Callback<DirectionsResult> callback = new Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    // Handle successful request.
                    myRoutes = result.routes;
                    if (myRoutes.length > 0) {
                        for (int r = 0; r < myRoutes.length; r++) {
                            System.out.println("route " + r + " : " + myRoutes[r].summary);
                            DirectionsRoute dr = myRoutes[r];
                            myLegs = dr.legs;
                            for (int l = 0; l < myLegs.length; l++) {
                                System.out.println("\tleg " + l + " : " + myLegs[l].startAddress + " - " + myLegs[l].endAddress);
                                mySteps = myLegs[l].steps;
                                myPolylines = new EncodedPolyline[mySteps.length];
                                instructions = new String[mySteps.length];
                                for (int s = 0; s < mySteps.length; s++) {
                                    System.out.println("\t\tstep " + s + " : " + mySteps[s].duration);
                                    myPolylines[s] = mySteps[s].polyline;
                                    instructions[s] = mySteps[s].htmlInstructions;
                                }
                            }
                        }
                    } else {
                        System.out.println("route vide");
                    }

                }

                @Override
                public void onFailure(Throwable e) {
                    // Handle error.
                    System.out.println("\t\tonFailure "+ e );
                }
            };
            request.setCallback(callback);

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }

            //Création de la map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }
        }

        else{}
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void stopUpdatesButtonHandler(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, GPS.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(GPS.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(GPS.this, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
                updateUI();
            }
        });

    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));
            mSpeedTextView.setText(String.format("%s: %s"+" km/h",mSpeedLabel,currentSpeed));
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                setButtonsEnabledState();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateUI();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateLocationUI();

            if (!originGiven){
                originLoc = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());

                //Crée la requête d'itinéraire
                GeoApiContext context = new GeoApiContext().setApiKey(getResources().getString(R.string.google_maps_directions));
                DirectionsApiRequest request = DirectionsApi.newRequest(context).origin(originLoc).destination(destination).language(language);
                // Envoie la requête de manière Asynchrone et stocke les résultats
                Callback<DirectionsResult> callback = new Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        // Handle successful request.
                        myRoutes = result.routes;
                        if (myRoutes.length > 0) {
                            for (int r = 0; r < myRoutes.length; r++) {
                                System.out.println("route " + r + " : " + myRoutes[r].summary);
                                DirectionsRoute dr = myRoutes[r];
                                myLegs = dr.legs;
                                for (int l = 0; l < myLegs.length; l++) {
                                    System.out.println("\tleg " + l + " : " + myLegs[l].startAddress + " - " + myLegs[l].endAddress);
                                    mySteps = myLegs[l].steps;
                                    myPolylines = new EncodedPolyline[mySteps.length];
                                    instructions = new String[mySteps.length];
                                    for (int s = 0; s < mySteps.length; s++) {
                                        System.out.println("\t\tstep " + s + " : " + mySteps[s].duration);
                                        myPolylines[s] = mySteps[s].polyline;
                                        System.out.println("\t\tpolylines "  + " : " + myPolylines[s]);
                                        instructions[s] = mySteps[s].htmlInstructions;
                                    }
                                }
                            }
                        } else {
                            System.out.println("route vide");
                        }

                    }

                    @Override
                    public void onFailure(Throwable e) {
                        // Handle error.
                        System.out.println("\t\tonFailure "+ e );
                    }
                };
                request.setCallback(callback);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                }

                //Création de la map
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }
            }
        }
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }

    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        positionConducteur();

        com.google.android.gms.maps.model.LatLng loc = new com.google.android.gms.maps.model.LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        myMap.addMarker(new MarkerOptions().position(loc).title("A"));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));
        updateLocationUI();

        procheConsigne();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Calcul de la vitesse
    private void getSpeed(Location newLocation, Location veryOldLocation){
        double newTime = System.currentTimeMillis()/1000;
        double newLat = newLocation.getLatitude();
        double newLon = newLocation.getLongitude();
        double oldLat = veryOldLocation.getLatitude();
        double oldLon = veryOldLocation.getLongitude();

        double distance = calculationByDistance(newLat,newLon,oldLat,oldLon);
        double timeDifferent = newTime - currentTime;
        currentSpeed = distance*3.6/timeDifferent;
        currentTime = newTime;
        double diffLat = abs(newLat-oldLat);
        double diffLong = abs(newLon-oldLon);
        System.out.println("\t\tspeed " + currentSpeed);
    }
    public double calculationByDistance(double lat1, double long1, double lat2, double long2){
        float[] res = new float[1];
        res[0] = 0;
        Location.distanceBetween(lat1, long1, lat2, long2, res);
        return (res[0]);


    }
    

    public void onMapReady(GoogleMap map){
        myMap = map;
        PolylineOptions polyOpt = new PolylineOptions();

        for (int s=0; s<myPolylines.length;s++) {

            List<LatLng> poly = myPolylines[s].decodePath();
            for (int i = 0; i < poly.size(); i++) {
                com.google.android.gms.maps.model.LatLng myLatLng = new com.google.android.gms.maps.model.LatLng(poly.get(i).lat, poly.get(i).lng);
                trajetPredit.add(myLatLng);
                polyOpt.add(myLatLng);
            }
        }

        debutStep = new Vector<>();
        for (int i=0; i<mySteps.length; i++){
            com.google.android.gms.maps.model.LatLng myStart = new com.google.android.gms.maps.model.LatLng(mySteps[i].startLocation.lat,mySteps[i].startLocation.lng);
            com.google.android.gms.maps.model.LatLng myEnd = new com.google.android.gms.maps.model.LatLng(mySteps[i].endLocation.lat,mySteps[i].endLocation.lng);
            myMap.addMarker(new MarkerOptions().position(myStart));
            myMap.addMarker(new MarkerOptions().position(myEnd));
            debutStep.add(myStart);
        }

        myMap.addPolyline(polyOpt);
        myMap.setMyLocationEnabled(true);

        //Création du troncon
        System.out.println("\t\ttrajetPredit"  + " : " + trajetPredit.size());
        float d = (float) calculationByDistance(trajetPredit.get(trajetPredit.size()-1).latitude, trajetPredit.get(trajetPredit.size()-1).longitude, trajetPredit.get(0).latitude, trajetPredit.get(0).longitude);
        t = new Troncon(0, d, 90, 70, 110, trajetPredit);
    }
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.FRANCE);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
    private void speakOut(String txtText) {
        tts.speak(txtText, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void positionConducteur(){
        int i = 0;
        double epsilon = (double) 3*10/36;
        int  conseil = 9;
        double dTot =0;
        double oldD =0;
        double newD = 0;
        double precision = Math.min(mCurrentLocation.getAccuracy(),oldLocation.getAccuracy());
        double distance = calculationByDistance(oldLocation.getLatitude(), oldLocation.getLongitude(), mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        System.out.println("\t\tposition lat"  + " : " + mCurrentLocation.getLatitude() + ", position long : "  + mCurrentLocation.getLongitude());
        System.out.println("\t\tancienne position lat"  + " : " + oldLocation.getLatitude() + ", position long : "  + oldLocation.getLongitude());
        System.out.println("\t\tdistance1"  + " : " + distance);
        System.out.println("\t\tPrecision : " + precision);
        if (distance > precision){
            System.out.println("\t\tboucle if");
            while (dTot < distance && oldD > newD){
                indiceDernierePos++;
                oldD = newD;
                newD = calculationByDistance(trajetPredit.get(indiceDernierePos).latitude, trajetPredit.get(indiceDernierePos).longitude,
                        mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
                dTot+= newD;
            };
            double dGauche = 0;
            if (indiceDernierePos > 0) {
                dGauche = Math.abs(calculationByDistance(trajetPredit.get(indiceDernierePos).latitude, trajetPredit.get(indiceDernierePos).longitude,
                        trajetPredit.get(indiceDernierePos - 1).latitude, trajetPredit.get(indiceDernierePos - 1).longitude) - newD);
            }
            double dDroite = Math.abs(calculationByDistance(trajetPredit.get(indiceDernierePos).latitude, trajetPredit.get(indiceDernierePos).longitude,
                    trajetPredit.get(indiceDernierePos + 1).latitude,trajetPredit.get(indiceDernierePos + 1).longitude) - newD);
            if (dGauche < dDroite && indiceDernierePos > 0) {
                indiceDernierePos -= 1;
            }

            getSpeed(mCurrentLocation, oldLocation);
            conseil = t.conseil(currentSpeed, oldSpeed, indiceDernierePos, epsilon);
            this.donnerConseil(conseil);
            oldLocation = mCurrentLocation;
        }
    }

    public void procheConsigne(){

        int indiceStep = indiceCurrentStep;
        double distanceConsigneNext;
        com.google.android.gms.maps.model.LatLng next= new com.google.android.gms.maps.model.LatLng(mySteps[indiceCurrentStep].endLocation.lat,mySteps[indiceCurrentStep].endLocation.lng);
        distanceConsigneNext = calculationByDistance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),mySteps[indiceCurrentStep].endLocation.lat,mySteps[indiceCurrentStep].endLocation.lng);
        System.out.println("\t\tconsigne"  + " : " + distanceConsigneNext);
        if (distanceConsigneNext<50){
            donnerConsigne(next,50);
            indiceCurrentStep++;
            consigne100 = true;
        }

        System.out.println("\t\tconsigne"  + " 2e if: " + mySteps[indiceCurrentStep].distance.inMeters);
        if(mySteps[indiceCurrentStep].distance.inMeters>150 & indiceStep==indiceCurrentStep){
            if(distanceConsigneNext<100 & consigne100){
                donnerConsigne(next,100);
                consigne100 = false;
            }
        }
    }
    public void donnerConseil(int conseil){
        Context context = getApplicationContext();
        String txt = "";
        // à compléter : conditions
        if (conseil == -3 | conseil == -2){
            txt = "Levez le pied";
            Toast toast = Toast.makeText(context, txt , Toast.LENGTH_LONG);
            speakOut(txt);
            toast.show();
        }

        if (conseil == -1){
            txt = "Accélerez";
            Toast toast = Toast.makeText(context, txt, Toast.LENGTH_LONG);
            speakOut(txt);
            toast.show();
        }

        if (conseil == 1){
            txt = "Ralentissez";
            Toast toast = Toast.makeText(context, txt, Toast.LENGTH_LONG);
            speakOut(txt);
            toast.show();
        }

        if (conseil == 2 | conseil == 3){
            txt = "Appuyez sur l'accélérateur";
            Toast toast = Toast.makeText(context, txt, Toast.LENGTH_LONG);
            speakOut(txt);
            toast.show();
        }

        if (conseil == 0){
            txt = "C'est parfait";
            Toast toast = Toast.makeText(context, txt, Toast.LENGTH_LONG);
            speakOut(txt);
            toast.show();
        }
    }
    
    //Permet de lire en audio les instructions            
    public void donnerConsigne(com.google.android.gms.maps.model.LatLng position, int dist){
        int index = debutStep.indexOf(position);
        String consigne = instructions[index];
        consigne = consigne.replaceAll("\\<.*?\\>", ""); //On enlève les tags HTML
        consigne = "À "+dist+"mètres, "+ consigne;
        System.out.println("\t\tconsigne"  + " : " + consigne);
        speakOut(consigne);
    }
}
