package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    //VISTAS
    EditText mEmailEt, mPasswordEt;
    TextView noTengoCuentaTv, mRecuperarTv;
    Button mLoginBTN;
    SignInButton mGoogleLoginBTN;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //PorgressDialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //BARRA DE ACCION Y SU TITULO
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        // HABILITAR BOTON RETROCESO
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //ANTES DE AUNTENTICAR
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //In the onCreate() method, initialize the FirebaseAuth instance.
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //INICIALIZAR LAS VARIABLES ENLAZANDOLA CON LOS CONTROLES POR MEDIO DE SU ID
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        noTengoCuentaTv = findViewById(R.id.NoTengo_CuentaTv);
        mLoginBTN = findViewById(R.id.BTN_Ingresar);
        mRecuperarTv = findViewById(R.id.RecuperarTv);
        mGoogleLoginBTN = findViewById(R.id.googleLoginBTN);
        //LOGGIN BUTTON CLICK
        mLoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // INPUT DATA
                String email = mEmailEt.getText().toString();
                String pass = mPasswordEt.getText().toString().trim();
                //VALIDACION
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                //INVALID EMAIL PATTERN -- ESTABLECE ERROR
                    mEmailEt.setError("Correo electrónico no válido");
                    mEmailEt.setFocusable(true);
                }else{
                // VALIDO
                    loginUser(email, pass);

                }
            }


        });
        // NO TIENES UNA CUENTA BUTTON CLICK
        noTengoCuentaTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //RECUPERAR CONTRASEÑA TEXTVIEW CLICK
        mRecuperarTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoveryPasswordDialog();
            }
        });

        //MANEJADOR GOOGLE LOGIN BTN CLICK
         mGoogleLoginBTN.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
            //EMPIEZA PROCESO DE LOGIN CON GOOGLE
                 Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                 startActivityForResult(signInIntent, RC_SIGN_IN);
             }
         });


        //INICIAR PROGRESSDIALOG
        progressDialog = new ProgressDialog(this);


    }

    private void showRecoveryPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");
        //Establece layout linear
        final LinearLayout linearLayout = new LinearLayout(this);

        //Vistas establcedias en el dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Ingrese su correo electrónico");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        // Establece limites max y min
        emailEt.setMinEms(16);


        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10 ,10);

        builder.setView(linearLayout);
        //BOTON RECUPERAR
        builder.setPositiveButton("Recuperar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // Input email
                String email = emailEt.getText().toString().trim();
                empiezaRecuperacion(email);
            }
        });
        //BOTON CANCELAR
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // Dismiss
                dialog.dismiss();
            }
        });

        //MUESTRA DIALOG
        builder.create().show();
    }

    private void empiezaRecuperacion(String email) {
        //Muestra progressDialog
        progressDialog.setMessage("Enviando correo de recuperación...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                //VALIDACION
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Correo enviado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this, "Fallo...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            //OBTENER Y MUESTRA ERROR
                progressDialog.dismiss();
                Toast.makeText( LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loginUser(String email, String pass) {
        //Muestra progressDialog
        progressDialog.setMessage("Ingresando...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //DIMISS PROGRESS
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // El usuario esta logueado, entonces COMIENZA LOGINACTICTIVITY
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //descartar
                progressDialog.dismiss();
                //error, obten el error y muestralo
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //ir a activity anterior
        return super.onSupportNavigateUp();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

                // ...
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            // Si un usuario entra por primera vez luego muestra y obtiene
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                //Obtener el email y el UID desde auth
                                String email = user.getEmail();
                                String uid = user.getUid();

                                //Cuando el usuario este registrado el almacenamiento de firebase realtime tambien
                                // estara usando HashMap
                                HashMap<Object, String> hashMap = new HashMap<>();
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

                            }


                            //Muestra el email en un toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();

                            // El usuario esta logueado, entonces COMIENZA LOGINACTICTIVITY
                            // IR A LA ACTIVIDAD DE PERFILDESP DE LOAGUAR
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Error al ingresar...", Toast.LENGTH_SHORT).show();

                           // updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //OBTEN Y MUESTRA EL ERROR
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}