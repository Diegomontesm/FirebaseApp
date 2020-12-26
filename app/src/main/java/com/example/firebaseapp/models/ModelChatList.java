package com.example.firebaseapp.models;

public class ModelChatList {

    String id; //Se requiere este id para obtener el chat list, enviar/recibir UID;

    public ModelChatList(String id) {
        this.id = id;
    }

    public ModelChatList() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
