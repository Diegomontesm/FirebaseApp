package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //VISTAS
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBTN;
    TextView mTengoCuenta;

    //Barra de progreso para mostrar mientras el usuario está registrandose
    ProgressDialog progressDialog;
    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //BARRA DE ACCION Y SU TITULO
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Crear cuenta");
        // HABILITAR BOTON RETROCESO
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //INICIAR
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt= findViewById(R.id.passwordEt);
        mRegisterBTN = findViewById(R.id.BTN_RegistrarUsuario);
        mTengoCuenta = findViewById(R.id.Tengo_CuentaTv);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando espere por favor...");

        //In the onCreate() method, initialize the FirebaseAuth instance.
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //MANEJAR REGISTRO DE USUARIO
        mRegisterBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //INPUT EMAIL, PASSWORD
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //Validacion
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //SET ERROR AND FOCUS TO EMAIL EDITTEXT
                    mEmailEt.setError("Correo electrónico no válido");
                    mEmailEt.setFocusable(true);
                }
                else if(password.length()<6){
                    //SET ERROR AND FOCUS TO PASSWORD EDITTEXT
                    mPasswordEt.setError("La contraseña debe tener al menos 6 caracteres");
                    mPasswordEt.setFocusable(true);
                }else{
                    registerUser(email, password); //REGISTRAR AL USUARIO
                }
            }
        });
        // Manejador login textview Click Listener
        mTengoCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        //El patron de email y contraseña son validos, mostrar progreso y empieza
        //a registrar al usuario
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Obtener el email y el UID desde auth
                            String email = user.getEmail();
                            String uid = user.getUid();

                            //Cuando el usuario este registrado el almacenamiento de firebase realtime tambien
                            // estara usando HashMap
                            HashMap<Object, String>hashMap = new HashMap<>();
                            //Poner info en hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("nombre", "");
                            hashMap.put("onlineStatus","online");
                            hashMap.put("escribiendoA","nadie");
                            hashMap.put("telefono", "");
                            hashMap.put("imagen", "");
                            hashMap.put("cover", "");

                            //Instancia de firebase base de datos
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // La ruta para almacenar informacion del usuario se llama "Usuarios"
                            DatabaseReference databaseReference = database.getReference("Usuarios");
                            // Poner informacion sin el hashmap en la bd
                            databaseReference.child(uid).setValue(hashMap);



                            Toast.makeText(RegisterActivity.this, "Registrado...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error, dismiss profress dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //ir a activity anterior
        return super.onSupportNavigateUp();
    }
}