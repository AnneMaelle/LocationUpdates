package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLng;

public class BilanTrajet extends AppCompatActivity implements OnMapReadyCallback {

    //UI Widgets
    protected TextView mScore;
    protected GoogleMap myMap;

    //Values
    protected int[] myRoadScores;
    protected int myTotalScore = 0;
    protected float[] myRoadLong;
    protected float[] myRoadLat;
    protected int myColor;

    Polyline myRoad ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilan_trajet);
        Toolbar bilanToolbar = (Toolbar) findViewById(R.id.bilan_toolbar);
        setSupportActionBar(bilanToolbar);

        mScore = (TextView) findViewById(R.id.score);

        //Création de la map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bilan_map);
        mapFragment.getMapAsync(this);

        //Récupération des données enregistrées dans GPS
        Intent myIntent = getIntent();
        myRoadLong = myIntent.getFloatArrayExtra("Longitudes");
        myRoadLat = myIntent.getFloatArrayExtra("Latitudes");
        myRoadScores = myIntent.getIntArrayExtra("Scores");

    }

    public void onMapReady(GoogleMap map){

        myMap = map;

        // Calcul du score et création du Path pour la Polyline
        myTotalScore += myRoadScores[0];
        for (int i = 1; i < myRoadScores.length; i++) {
            myTotalScore += myRoadScores[i];
            if (myRoadScores[i] > 5){
                myColor = Color.GREEN ; //Hexadecimal code : (0xff00ff00)
            }
            else if (myRoadScores[i] < -5){
                myColor = Color.RED ; //Hexadecimal code : (0xffff0000)
            }
            else if (5 <= myRoadScores[i] || myRoadScores[i] <= 5){
                myColor = Color.GRAY; //Hexadecimal code : (0xffcccccc)
            }
            myRoad = myMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(myRoadLat[i-1],myRoadLong[i-1]),new LatLng(myRoadLat[i],myRoadLong[i]))
                    .color(myColor));
        }
    }
}
