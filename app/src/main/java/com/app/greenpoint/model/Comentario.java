package com.app.greenpoint.model;

public class Comentario {

    private int idComentario;
    private int idContenedor;
    private int tipoContenedor;
    private String nomUsuario;
    private String fecha;
    private String texto;
    private String encodedImg;

    public Comentario() {
    }

    public Comentario(int idComentario, int idContenedor, int tipoContenedor, String nomUsuario, String fecha, String texto) {
        this.idComentario = idComentario;
        this.idContenedor = idContenedor;
        this.tipoContenedor = tipoContenedor;
        this.nomUsuario = nomUsuario;
        this.fecha = fecha;
        this.texto = texto;
    }

    public String getEncodedImg() {
        return encodedImg;
    }

    public void setEncodedImg(String encodedImg) {
        this.encodedImg = encodedImg;
    }

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public int getIdContenedor() {
        return idContenedor;
    }

    public void setIdContenedor(int idContenedor) {
        this.idContenedor = idContenedor;
    }

    public int getTipoContenedor() {
        return tipoContenedor;
    }

    public void setTipoContenedor(int tipoContenedor) {
        this.tipoContenedor = tipoContenedor;
    }

    public String getNomUsuario() {
        return this.nomUsuario;
    }

    public void setNomUsuario(String nomUsuario) {
        this.nomUsuario = nomUsuario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
