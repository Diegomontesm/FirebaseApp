package com.example.firebaseapp.models;

public class ModelComment {
    String cId, comentario, timestamp, uid, uEmail, uDp, uNombre;

    public ModelComment() {
    }

    public ModelComment(String cId, String comentario, String timestamp, String uid, String uEmail, String uDp, String uNombre) {
        this.cId = cId;
        this.comentario = comentario;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uNombre = uNombre;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
