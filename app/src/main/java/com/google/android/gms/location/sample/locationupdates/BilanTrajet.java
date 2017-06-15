package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.google.android.gms.maps.CameraUpdateFactory;
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
    protected float[] myRoadScores;
    protected int myTotalScore = 0;
    protected int myColor;
    protected Trajet trajet;
    protected PolylineOptions[] polylineOptions;
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
        trajet = myIntent.getParcelableExtra("Trajet");
        myRoadScores = trajet.listeNoteVar;
        polylineOptions = trajet.polylineOptions;
    }

    public void onMapReady(GoogleMap map){

        myMap = map;

        // Calcul du score et création de la Polyline
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

            polylineOptions[i].color(myColor);
            myRoad = myMap.addPolyline(polylineOptions[i]);
        }
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trajet.getTroncons().lastElement().positionsConnues.lastElement(), 10));
    }
}
