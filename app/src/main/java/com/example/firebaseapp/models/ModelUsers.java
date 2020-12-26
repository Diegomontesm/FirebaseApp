package com.example.firebaseapp.models;

public class ModelUsers {
    //Usa el mismo nombre que el de firebase DB
    String cover, email, imagen, nombre, telefono, uid, onlineStatus, typingTo;

    public ModelUsers(){

    }

    public ModelUsers(String cover, String email, String imagen, String nombre, String telefono, String uid, String onlineStatus, String typingTo) {
        this.cover = cover;
        this.email = email;
        this.imagen = imagen;
        this.nombre = nombre;
        this.telefono = telefono;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
