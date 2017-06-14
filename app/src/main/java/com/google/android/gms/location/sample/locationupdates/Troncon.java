package com.google.android.gms.location.sample.locationupdates;

/**
 * Created by Adele on 01/05/2017.
 */

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Vector;

public class Troncon {

    public int indice;
    public float dTroncon;
    private float vlim;
    private float v0;
    private float v2; //paramètres à récupérer grâce à l'API <google Maps
    private float Kc; //constante du véhicule
    private float a0, a2; //accélaration calculée par optimisation
    private int indice1, indice2;
    private float pas;
    public Vector<LatLng> positionsConnues;
    public float EIdeal, EReel; //CO2 rejeté
    public float noteCO2, noteVar;

    public Troncon(int i, float d, float vitLim, float vit0, float vit2, Vector<LatLng> positions) {
        indice = i;
        dTroncon = d;
        vlim = vitLim;
        v0 = vit0;
        v2 = vit2;
        Kc = (float) 1;
        a0 = (vlim - v0) / 30;
        a2 = (vlim - v2) / 30;
        positionsConnues = positions;
        this.profilDeVitesse();
        this.calculEIdeal();
    }

    public double calculationByDistance(double lat1, double long1, double lat2, double long2){
        //permet de calculer la distance entre deux positios GPS
        float[] res = new float[1];
        res[0] = 0;
        Location.distanceBetween(lat1, long1, lat2, long2, res);
        return (res[0]);
    }

    private void profilDeVitesse(){
        //permet de connaître le profil idéal de vitesse en avance
        if (v0<vlim){
            double T1 = (double) (vlim-v0)/a0;
            double d1 = (0.5*a0*T1*T1 + v0*T1);
            double d = 0;
            indice1 = 0;
            while (d < d1 && indice1 < positionsConnues.size()){

                d += calculationByDistance(positionsConnues.get(indice1).latitude, positionsConnues.get(indice1).longitude,
                        positionsConnues.get(indice1 + 1).latitude, positionsConnues.get(indice1 + 1).longitude);
                indice1++;
            }
            if (v2 > vlim){
                indice2 = (int) Double.NaN;
            }
            else{
                int indice2 = indice1;
                double T2 = (double) (dTroncon -(vlim*vlim-v2*v2)/(2*a2)+Math.pow(vlim-v0, 2)/(2*a0))/vlim;
                double d2 = (double) (vlim*(T2-T1)+d1);
                while (d < d2 && indice2 < positionsConnues.size()){
                    d += calculationByDistance(positionsConnues.get(indice2).latitude, positionsConnues.get(indice2).longitude,
                            positionsConnues.get(indice2 + 1).latitude, positionsConnues.get(indice2 + 1).longitude);
                    indice2++;
                }
            }
        } else {
            indice1 = (int) Double.NaN;
            if (v2 < vlim){
                double T2 = (dTroncon - (vlim*vlim - v2*v2)/(2*a2))/vlim;
                double d2 = vlim * T2;
                double d = 0;
                int indice2 = 0;
                while (d < d2 && indice2 < positionsConnues.size()){
                    d += calculationByDistance(positionsConnues.get(indice2).latitude, positionsConnues.get(indice2).longitude,
                            positionsConnues.get(indice2 + 1).latitude, positionsConnues.get(indice2 + 1).longitude);
                    indice2++;
                }
            } else {
                indice2 = (int) Double.NaN;
            }
        }
    }

    private void calculEIdeal(){
        float Tmax;
        if (v0<vlim){
            if (v2>vlim){ //cas n1
                Tmax = (float) (dTroncon - Math.pow(vlim-v0, 2)/(2*a0))/vlim;
            } else{ //cas n2
                Tmax = (float) (dTroncon + Math.pow(vlim-v2,2)/(2*a2)+Math.pow(vlim-v0, 2)/(2*a0))/vlim;
            }
            EIdeal = (float) (Kc*(0.3*Tmax + 0.028*dTroncon + 0.056*(Math.pow(vlim - v0, 2)/a0 - 2*v0*(vlim-v0))));
        } else {
            if (v2<vlim){ //cas n3
                Tmax = (float) ((dTroncon + Math.pow(vlim-v2, 2)/(2*a2))/vlim);
            } else { //cas n4
                Tmax = dTroncon/vlim;
            }
            EIdeal = (float) (Kc*(0.3*Tmax + 0.028*dTroncon));
        }
    }

    public int conseil(double vitesseActuelle, double vitessePrecedente, int indiceActuel, double epsilon){
        // retourne : 0 si la conduite est bien,
        // -1 si la vitesse est trop basse (en phase de vitesse constante)
        // 1 si la vitesse est trop élevée (en phase de vitesse constante)
        // -2 si la décélération n'et pas assez élevée
        // 2 si la décélération est trop élevé
        // -3 si l'accélération n'est pas assez élevée
        // 3 si l'accélération est trop élevée.

        //met à jour la note

        double sigma = 0;

        //calcul du taux de CO2 rejeté
        if (vitessePrecedente < vitesseActuelle){
            sigma = vitesseActuelle*vitesseActuelle - vitessePrecedente*vitessePrecedente;
        }
        EReel = (float) (EReel + Kc*(0.3*pas + 0.028*pas*Math.abs(vitesseActuelle-vitessePrecedente)+sigma));
        noteCO2 = EIdeal/EReel;
        // calcul de la note "constance de la vitesse


        if ((Double.isNaN((double)indice1))){
            if ((Double.isNaN((double)indice2))){ //cas 4
                if (Math.abs(vitessePrecedente-vitesseActuelle) < 1.*1000./3600.){
                    noteVar = noteVar +1;
                }
                if (vitesseActuelle > vlim + epsilon){
                    return(1);
                } else if (vitesseActuelle > vlim - epsilon){
                    return(-1);
                } else{
                    return(0);
                }

            } else { //cas 3
                if (indiceActuel < indice2){ //vitesse constante
                    if (Math.abs(vitessePrecedente-vitesseActuelle) < 1.*1000./3600.){
                        noteVar = noteVar +1 ;
                    }
                    if (vitesseActuelle > vlim + epsilon){
                        return(1);
                    } else if (vitesseActuelle > vlim - epsilon){
                        return(-1);
                    } else{
                        return(0);
                    }
                } else { //décélération
                    double dec = (vitesseActuelle - vitessePrecedente)/pas;
                    if (dec > a2 + epsilon){ // !!!!Attention au signe, à revoir
                        return (2);
                    } else if ( dec < a2 - epsilon){
                        return(-2);
                    } else {
                        return(0);
                    }
                }
            }
        } else {
            if (((Double.isNaN((double)indice2)))){ //cas 1
                if (Math.abs(vitessePrecedente-vitesseActuelle) < 1.*1000./3600.){
                    noteVar = noteVar +1;
                }
                if (indiceActuel > indice1){ //vitesse constante
                    if (vitesseActuelle > vlim + epsilon){
                        return(1);
                    } else if (vitesseActuelle > vlim - epsilon){
                        return(-1);
                    } else{
                        return(0);
                    }
                } else { //accélération

                }
            } else { //cas 2
                if (indiceActuel < indice1){ //acc
                    double acc = (vitesseActuelle - vitessePrecedente)/pas;
                    if (acc > a2 + epsilon){
                        return (3);
                    } else if ( acc < a2 - epsilon){
                        return(-3);
                    } else {
                        return(0);
                    }
                } else {
                    if (indiceActuel < indice2){ //vitesse constante
                        if (Math.abs(vitessePrecedente-vitesseActuelle) < 1.*1000./3600.){
                            noteVar = noteVar +1;
                        }
                        if (vitesseActuelle > vlim + epsilon){
                            return(1);
                        } else if (vitesseActuelle > vlim - epsilon){
                            return(-1);
                        } else{
                            return(0);
                        }
                    } else { //décéleration
                        double dec = (vitesseActuelle - vitessePrecedente)/pas;
                        if (dec > a2 + epsilon){ //decélération
                            return (2);
                        } else if ( dec < a2 - epsilon){
                            return(-2);
                        } else {
                            return(0);
                        }
                    }
                }
            }
        }
        return (0);
    }

    public int getIndice1(){
        if (indice1 != Double.NaN) {
            return(indice1);
        } else {
            return(0);
        }

    }

        public int getIndice2(){
            if (indice2 != Double.NaN) {
                return(indice2);
            } else {
                return(positionsConnues.size());
        }
    }


    public float getdTroncon(){
        return dTroncon;
    }

    public void setPas(float timeDifference){
        pas = timeDifference;
    }

}

