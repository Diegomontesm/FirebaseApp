package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //VISTAS
    Button mRegisterBTN, mLoginBTN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //INICIALIZAR VISTAS
        mRegisterBTN = findViewById(R.id.BTN_Registrar);
        mLoginBTN = findViewById(R.id.BTN_Login);

        //MANEJADOR DEL BOTON REGISTRAR
        mRegisterBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EMPEZAR REGISTERACTI
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            }
        });

        //MANEJADOR DEL BOTON LOGIN
        //HANDLE LOGIN BUTTON CLICK
        mLoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EMPIEZA LOGINACTIVITY
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}