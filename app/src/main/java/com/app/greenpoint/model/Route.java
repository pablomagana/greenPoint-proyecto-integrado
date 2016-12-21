package com.app.greenpoint.model;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Route {

    private String distance;
    private String time;
    private ArrayList<LatLng> arrayList = new ArrayList<>();

    public Route() {
    }

    public Route(String distance, String time, ArrayList<LatLng> arrayList) {
        this.distance = distance;
        this.time = time;
        this.arrayList = arrayList;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<LatLng> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<LatLng> arrayList) {
        this.arrayList = arrayList;
    }
}
