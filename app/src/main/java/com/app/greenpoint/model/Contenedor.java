package com.app.greenpoint.model;


import com.google.android.gms.maps.model.LatLng;

public class Contenedor {
    private int id;
    private int tipo;
    private String direccion;
    private LatLng location;

    public Contenedor() {

    }

    public Contenedor(int id, int tipo, String direccion, LatLng location) {
        this.id = id;
        this.tipo = tipo;
        this.direccion = direccion;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
