package com.app.greenpoint.model;

import com.google.android.gms.maps.model.Marker;

public class MarkerContenedor {

    private Contenedor contenedor;
    private Marker marker;

    public MarkerContenedor(Contenedor contenedor, Marker marker) {
        this.contenedor = contenedor;
        this.marker = marker;
    }

    public boolean containsMarker(Marker m) {
        return this.marker.equals(m);
    }

    public boolean containsContenedor(Contenedor c) {
        return this.contenedor.equals(c);
    }

    public Contenedor getContenedor() {
        return contenedor;
    }

    public void setContenedor(Contenedor contenedor) {
        this.contenedor = contenedor;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
