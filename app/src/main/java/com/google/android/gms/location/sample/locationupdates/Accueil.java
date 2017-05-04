package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class Accueil extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        Toolbar homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);
    }

    public void goItin(View myView){
        Intent itinIntent = new Intent(this, Itineraire.class);
        startActivity(itinIntent);
    }
}
