package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class Itineraire extends AppCompatActivity implements View.OnKeyListener {

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
        toPoint = (EditText) findViewById(R.id.to) ;
        toPoint.setOnKeyListener(this);

        fromPoint = (EditText) findViewById(R.id.from) ;
        fromPoint.setOnKeyListener(this);
    }

    public boolean onKey(View myView, int keyCode, KeyEvent event){
        if(keyCode==EditorInfo.IME_ACTION_SEARCH || keyCode==EditorInfo.IME_ACTION_DONE || event.getAction()==KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            if(!event.isShiftPressed()){
                switch(myView.getId()){
                    case R.id.from:
                        break;
                    case R.id.to:
                        break;
                }
                return true;
            }
        }
        return false;
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
