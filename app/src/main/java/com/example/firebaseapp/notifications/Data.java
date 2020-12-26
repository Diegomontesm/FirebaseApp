package com.example.firebaseapp.notifications;

public class Data {
    private String usuario, cuerpo, titulo, enviado;
    private Integer icono;


    public Data() {
    }

    public Data(String usuario, String cuerpo, String titulo, String enviado, Integer icono) {
        this.usuario = usuario;
        this.cuerpo = cuerpo;
        this.titulo = titulo;
        this.enviado = enviado;
        this.icono = icono;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEnviado() {
        return enviado;
    }

    public void setEnviado(String enviado) {
        this.enviado = enviado;
    }

    public Integer getIcono() {
        return icono;
    }

    public void setIcono(Integer icono) {
        this.icono = icono;
    }
}


