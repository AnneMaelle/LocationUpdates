package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class Itineraire extends AppCompatActivity {

    private EditText fromPoint ;
    private EditText toPoint;
    private String origin ;
    private String destination ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraire);

        Toolbar itineraryToolbar = (Toolbar) findViewById(R.id.itinerary_toolbar);
        setSupportActionBar(itineraryToolbar);
    }

    public void goNoItineraryGPS(View myView){
        Intent gpsIntent = new Intent(this, GPS.class);
        startActivity(gpsIntent);
    }
    public void goGPS(View myView){
        fromPoint = (EditText) findViewById(R.id.from) ;
        origin = fromPoint.getText().toString();

        toPoint = (EditText) findViewById(R.id.to) ;
        destination = toPoint.getText().toString();

        Intent gpsIntent = new Intent(this, GPS.class);
        gpsIntent.putExtra("Origine",origin);
        gpsIntent.putExtra("Destination",destination);
        startActivity(gpsIntent);
    }
}
