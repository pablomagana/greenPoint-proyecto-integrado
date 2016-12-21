package com.app.greenpoint.model;

import java.util.ArrayList;

public class Semanal extends Datos{

    private ArrayList<Reciclaje> datos = new ArrayList<>();

    public Semanal() {
    }

    public Reciclaje getReciclajeTipo(int tipo) {
        for (Reciclaje r : datos) {
            if (r.getTipo().equals("" + tipo))
                return r;
        }
        return null;
    }

    public ArrayList<Reciclaje> getDatos() {
        return datos;
    }

    public void setDatos(ArrayList<Reciclaje> datos) {
        this.datos = datos;
    }
}
