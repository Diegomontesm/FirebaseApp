package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.firebaseapp.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    //FIREBASE AUTENTICACION
    FirebaseAuth firebaseAuth;

    //VIASTAS
    //TextView mProfileTv;
    ActionBar actionBar;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //BARRA DE ACCION Y SU TITULO
        actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");

        //INICIALIZAR
        firebaseAuth = FirebaseAuth.getInstance();

        //INIT VIEWS
        // mProfileTv = findViewById(R.id.perfilTv);

        //BOTON NAVIGATION
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        // Home fragment transaction (DEFAULT. ON START)
        actionBar.setTitle("Home");//Cambiar el titulo del actionbar
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, homeFragment, "");
        ft1.commit();

        RevisarEstatusUsuario();
     }

    @Override
    protected void onResume() {
        RevisarEstatusUsuario();
        super.onResume();
    }

    public void actualizarToken(String token){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
            Token mToken = new Token(token);
            ref.child(mUID).setValue(mToken);
        }



    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Manejador de los clicks
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            // Home fragment transaction
                            actionBar.setTitle("Home");//Cambiar el titulo del actionbar
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, homeFragment, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            // Profile transaction
                            // Perfil fragment transaction
                            actionBar.setTitle("Perfil");//Cambiar el titulo del actionbar
                            ProfileFragment perfilFragment = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, perfilFragment, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            // Users transaction
                            // Usuarios fragment transaction
                            actionBar.setTitle("Usuarios");//Cambiar el titulo del actionbar
                            UserFragment userFragment = new UserFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, userFragment, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            // Users transaction
                            // Chat fragment transaction
                            actionBar.setTitle("Chat");//Cambiar el titulo del actionbar
                            ChatListFragment chatListFragment = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, chatListFragment, "");
                            ft4.commit();
                            return true;
                    }
                    return false;
                }
            };


    private void RevisarEstatusUsuario(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //VALIDACION
        if(user != null){
            //user is signed and stay here
            // Establece emaildel logueado
           // mProfileTv.setText(user.getEmail());
            mUID = user.getUid();
            //GUARDAR EL UID DEL USUARIO ACTUALMENTE LOAGUEADO EN PREFERENCIAS COMPARTIDAS
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            // Actualizar token
            actualizarToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            //user isnt signed fo to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //Check on start f app
        RevisarEstatusUsuario();
        super.onStart();
    }

}