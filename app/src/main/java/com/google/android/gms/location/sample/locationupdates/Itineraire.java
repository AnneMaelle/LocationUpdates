package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class Itineraire extends AppCompatActivity implements View.OnKeyListener{

    private EditText fromPoint ;
    private EditText toPoint;
    private String origin ;
    private String destination ;
    private boolean originGiven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraire);

        Toolbar itineraryToolbar = (Toolbar) findViewById(R.id.itinerary_toolbar);
        setSupportActionBar(itineraryToolbar);

        originGiven = true;
        fromPoint = (EditText) findViewById(R.id.from) ;
        fromPoint.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int Enter, KeyEvent event) {
                
                return false;
            }
        });
        toPoint = (EditText) findViewById(R.id.to) ;
        fromPoint.setText("");
    }

    public void goNoItineraryGPS(View myView){
        Intent gpsIntent = new Intent(this, GPS.class);
        startActivity(gpsIntent);
    }

    public void originGivenMethod(View myView){
        originGiven = false;
        fromPoint.setText("Ma position");
    }

    public void goGPS(View myView){
        origin = fromPoint.getText().toString();
        destination = toPoint.getText().toString();

        Intent gpsIntent = new Intent(this, GPS.class);
        gpsIntent.putExtra("Origine",origin);
        gpsIntent.putExtra("Destination",destination);
        gpsIntent.putExtra("Origine donnée", originGiven);
        startActivity(gpsIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        originGiven = true;
        fromPoint.setText("");
        toPoint.setText("");
    }
}
