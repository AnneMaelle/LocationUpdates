package com.google.android.gms.location.sample.locationupdates;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Vector;
import com.google.android.gms.location.sample.locationupdates.Troncon;
import com.google.android.gms.maps.model.LatLng;

public class Trajet implements Parcelable{
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
		double distanceTotale=0;
		double distanceCst = 0;
		for (int i=0; i<nombreTroncon; i++){
			t=troncons.elementAt(i);
			EIdeal += t.EIdeal;
			EReel += t.EReel;

			noteVar += t.noteVar*(t.getIndice2() - t.getIndice1());
			noteCO2 += t.getdTroncon()*t.noteCO2;
			distanceTotale += t.getdTroncon();
			distanceCst += t.getIndice2()-t.getIndice1();
			if (noteCO2 > 1){
				noteCO2 = 100;
			}else {
				noteCO2 = noteCO2*100;
			}
			noteVar = ( float ) (noteVar / distanceCst);
		}
		
	}

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(troncons);
        dest.writeInt(nombreTroncon);
        dest.writeFloat(EIdeal);
        dest.writeFloat(EReel);
        dest.writeFloat(noteCO2);
        dest.writeFloat(noteVar);
    }

    protected static final Parcelable.Creator<Trajet> CREATOR = new Parcelable.Creator<Trajet>() {
        @Override
        public Trajet createFromParcel(Parcel source) {
            return new Trajet(source);
        }

        @Override
        public Trajet[] newArray(int size) {
            return new Trajet[size];
        }
    };

    protected Trajet(Parcel in){
        this.troncons = new Vector<>();
        in.readTypedList(troncons,Troncon.CREATOR);
        this.EIdeal = in.readFloat();
        this.EReel = in.readFloat();
        this.nombreTroncon = in.readInt();
        this.noteCO2 = in.readFloat();
        this.noteVar = in.readFloat();

    }
}
