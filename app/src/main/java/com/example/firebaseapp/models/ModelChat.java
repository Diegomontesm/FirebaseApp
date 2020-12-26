package com.example.firebaseapp.models;

public class ModelChat {
    String mensaje, receptor, enviador, timestamp;
    boolean fueVisto;

    public ModelChat() {
    }

    public ModelChat(String mensaje, String receptor, String enviador, String timestamp, boolean fueVisto) {
        this.mensaje = mensaje;
        this.receptor = receptor;
        this.enviador = enviador;
        this.timestamp = timestamp;
        this.fueVisto = fueVisto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getReceptor() {
        return receptor;
    }

    public void setReceptor(String receptor) {
        this.receptor = receptor;
    }

    public String getEnviador() {
        return enviador;
    }

    public void setEnviador(String enviador) {
        this.enviador = enviador;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFueVisto() {
        return fueVisto;
    }

    public void setFueVisto(boolean fueVisto) {
        this.fueVisto = fueVisto;
    }
}
