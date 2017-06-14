package com.google.android.gms.location.sample.locationupdates;

import java.util.Vector;

import com.google.android.gms.location.sample.locationupdates.Troncon;
import com.google.android.gms.maps.model.LatLng;

public class Trajet {
	private Vector<Troncon> troncons;
	int nombreTroncon;
	float EIdeal, EReel;
	float noteCO2, noteVar;
	
	public Trajet(){
		troncons = new Vector<Troncon>();
		EIdeal = 0;
		EReel = 0;
	}
	
	public void ajouterTroncon(float d, float vitLim, float vit0, float vit2, Vector<LatLng> positionsGPS){
		//a travailler avec l'api maps
		Troncon t = new Troncon(nombreTroncon, d, vitLim, vit0, vit2, positionsGPS);
		troncons.addElement(t);
		nombreTroncon ++;
	}
	
	public Vector<Troncon> getTroncons(){
		return(troncons);
	}
	
	public void finDeTrajet(){
		Troncon t;
		double distanceTotale = 0;
		for (int i=0; i<nombreTroncon; i++){
			t=troncons.elementAt(i);
			EIdeal += t.EIdeal;
			EReel += t.EReel;
			noteCO2 += (double) t.dTroncon*t.noteCO2;
			noteVar += t.dTroncon*t.noteVar/(t.getIndice2() - t.getIndice1());
			distanceTotale += t.dTroncon;
			if (noteCO2 <= 1){
				noteCO2 = 100;
			}else if (noteCO2 >= 2){
				noteCO2 = 0;
			} else {
				noteCO2 = (float) Math.floor( 100*noteCO2 - 100);
			}
			noteVar = ( float ) (noteVar / distanceTotale);
		}
		
	}
	
}
