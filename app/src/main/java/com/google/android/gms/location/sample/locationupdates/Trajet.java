package com.google.android.gms.location.sample.locationupdates;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Vector;
import com.google.android.gms.location.sample.locationupdates.Troncon;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class Trajet implements Parcelable{
	private Vector<Troncon> troncons;
    float[] listeNoteVar;
	int nombreTroncon;
	float EIdeal, EReel;
	float noteCO2, noteVar;
    PolylineOptions[] polylineOptions;
	
	public Trajet(){
		troncons = new Vector<Troncon>();
		EIdeal = 0;
		EReel = 0;
	}
	
	public void ajouterTroncon(Troncon t){
		//a travailler avec l'api maps
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

            listeNoteVar[i]=t.noteVar;
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

            polylineOptions[i] = t.polylineOptions[0];
		}
		
	}

	//Permet de faire passer un Trajet avec un intent
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(troncons);
        dest.writeTypedArray(polylineOptions,0);
        dest.writeInt(nombreTroncon);
        dest.writeFloat(EIdeal);
        dest.writeFloat(EReel);
        dest.writeFloat(noteCO2);
        dest.writeFloat(noteVar);
        dest.writeFloatArray(listeNoteVar);
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
        this.listeNoteVar = new float[troncons.size()];
        in.readFloatArray(listeNoteVar);
        this.polylineOptions = new PolylineOptions[troncons.size()];
        in.readTypedArray(polylineOptions,PolylineOptions.CREATOR);

    }
}
