package com.app.greenpoint.model;


public class Usuario {

    private String nombre;
    private String correo;
    private String claveApi;
    private String imagen;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String claveApi) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveApi = claveApi;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClaveApi() {
        return claveApi;
    }

    public void setClaveApi(String claveApi) {
        this.claveApi = claveApi;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
