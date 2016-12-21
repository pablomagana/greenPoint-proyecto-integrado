package com.app.greenpoint.model;

public class Favorito {

    private int idFavorito;
    private Contenedor contenedor;

    public Favorito() {
    }

    public Favorito(int idFavorito, Contenedor contenedor) {
        this.idFavorito = idFavorito;
        this.contenedor = contenedor;
    }

    public int getIdFavorito() {
        return idFavorito;
    }

    public void setIdFavorito(int idFavorito) {
        this.idFavorito = idFavorito;
    }

    public Contenedor getContenedor() {
        return contenedor;
    }

    public void setContenedor(Contenedor contenedor) {
        this.contenedor = contenedor;
    }
}
