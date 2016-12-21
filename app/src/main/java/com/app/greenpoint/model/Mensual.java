package com.app.greenpoint.model;


import java.util.ArrayList;

public class Mensual extends Datos {

    private ArrayList<Reciclaje> datos = new ArrayList<>();
    private String numMes;

    public Mensual() {
    }

    public Reciclaje getReciclajeTipo(int tipo) {
        for (Reciclaje r : datos) {
            if (r.getTipo().equals("" + tipo))
                return r;
        }
        return null;
    }

    public String getNumMes() {
        return numMes;
    }

    public void setNumMes(String numMes) {
        this.numMes = numMes;
    }

    public ArrayList<Reciclaje> getDatos() {
        return datos;
    }

    public void setDatos(ArrayList<Reciclaje> datos) {
        this.datos = datos;
    }
}
