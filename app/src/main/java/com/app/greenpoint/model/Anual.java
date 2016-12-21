package com.app.greenpoint.model;

import java.util.ArrayList;

public class Anual extends Datos {

    private ArrayList<?>[] datos = new ArrayList[12];

    public Anual() {

    }

    public Reciclaje getReciclajeTipo(int tipo, int mes) {
        ArrayList<?> mensual = datos[mes];
        for (Object o : mensual) {
            Reciclaje r = (Reciclaje) o;
            if (r.getTipo().equals(""+tipo)){
                return r;
            }
        }
        return null;
    }

    public double getMediaMes(int mes) {
        ArrayList<?> mensual = datos[mes];
        double suma = 0.0;
        for (Object o : mensual) {
            Reciclaje r = (Reciclaje) o;
            suma += r.getCantidad();
        }
        return suma/mensual.size();
    }

    public ArrayList<?> getDatosMes(int mes) {
        return this.datos[mes];
    }

    public void setDatosMes(int mes, ArrayList<Reciclaje> datos) {
        this.datos[mes] = datos;
    }

    public ArrayList<?>[] getDatos() {
        return datos;
    }

    public void setDatos(ArrayList<?>[] datos) {
        this.datos = datos;
    }
}
