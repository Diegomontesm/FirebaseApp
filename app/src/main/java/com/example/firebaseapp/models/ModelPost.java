package com.example.firebaseapp.models;

public class ModelPost {
    //Usaremos los mismos nombres que le dimos mientras cargaba el post
    String pId, pTitulo, pDescripcion, pLikes, pComentarios, pImagen, pHora, uid, uEmail, uDp, uNombre;

    //Constructor vacio
    public ModelPost() {
    }

    //Constructor

    public ModelPost(String pId, String pTitulo, String pDescripcion, String pLikes, String pComentarios, String pImagen, String pHora, String uid, String uEmail, String uDp, String uNombre) {
        this.pId = pId;
        this.pTitulo = pTitulo;
        this.pDescripcion = pDescripcion;
        this.pLikes = pLikes;
        this.pComentarios = pComentarios;
        this.pImagen = pImagen;
        this.pHora = pHora;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uNombre = uNombre;
    }


    //GETTERS AND SETTERS
    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitulo() {
        return pTitulo;
    }

    public void setpTitulo(String pTitulo) {
        this.pTitulo = pTitulo;
    }

    public String getpDescripcion() {
        return pDescripcion;
    }

    public void setpDescripcion(String pDescripcion) {
        this.pDescripcion = pDescripcion;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComentarios() {
        return pComentarios;
    }

    public void setpComentarios(String pComentarios) {
        this.pComentarios = pComentarios;
    }

    public String getpImagen() {
        return pImagen;
    }

    public void setpImagen(String pImagen) {
        this.pImagen = pImagen;
    }

    public String getpHora() {
        return pHora;
    }

    public void setpHora(String pHora) {
        this.pHora = pHora;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuNombre() {
        return uNombre;
    }

    public void setuNombre(String uNombre) {
        this.uNombre = uNombre;
    }
}
