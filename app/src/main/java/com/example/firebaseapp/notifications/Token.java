package com.example.firebaseapp.notifications;

public class Token {
    /* Un FCM Token, o mejor conocido como registrationToken. Un ID emitido por el GMC conexión de
    servidores para que la app del cliente permita recibir mensajes     */

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
