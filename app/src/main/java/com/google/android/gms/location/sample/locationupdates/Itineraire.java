package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class Itineraire extends AppCompatActivity implements TextWatcher {

    private EditText fromPoint ; // zone de saisie du point de départ
    private EditText toPoint; // zone de saisie du point d'arrivée
    private String origin ; // point de départ à envoyer à l'activité GPS
    private String destination ; // destination à envoyer à l'activité GPS
    private boolean originGiven; // permettra d'envoyer l'information que le point de départ est "ma position" à l'activité GPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraire);

        Toolbar itineraryToolbar = (Toolbar) findViewById(R.id.itinerary_toolbar);
        setSupportActionBar(itineraryToolbar);

        originGiven = true;
        toPoint = (EditText) findViewById(R.id.to) ;
        fromPoint = (EditText) findViewById(R.id.from) ;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count){
        System.out.println("\t\tonTextchanged"  + " : " + s);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        CharSequence s = editable.
        System.out.println("\t\tonTextchanged"  + " : " + s);
    }

    /*public boolean onKey(View myView, int keyCode, KeyEvent event){
        System.out.println("\t\tkeyCode"  + " : " + event.getKeyCode());
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
<<<<<<< Updated upstream
    }
    
    // Passage à l'activité GPS sans itinéraire
    // Pour l'instant, renvoie une erreur car l'activité suivante calcule un itinéraire sur le onCreate
=======
    }*/

>>>>>>> Stashed changes
    public void goNoItineraryGPS(View myView){
        Intent gpsIntent = new Intent(this, GPS.class);
        startActivity(gpsIntent);
    }

    // Permet l'utilisation de "ma position" comme point de départ
    public void originGivenMethod(View myView){
        originGiven = false;
        fromPoint.setText("Ma position");
    }

    // Passage à l'activité GPS en utilisant un point de départ et une destination
    public void goGPS(View myView){
        origin = fromPoint.getText().toString();
        destination = toPoint.getText().toString();
        Intent gpsIntent = new Intent(this, GPS.class);
        gpsIntent.putExtra("Origine",origin);
        gpsIntent.putExtra("Destination",destination);
        gpsIntent.putExtra("Origine donnée", originGiven);
        startActivity(gpsIntent);
    }

    // Réinitialisation des points de départ et d'arrivée lors du retour sur cette activité
    @Override
    protected void onResume() {
        super.onResume();
        originGiven = true;
        fromPoint.setText("");
        toPoint.setText("");
    }
}
