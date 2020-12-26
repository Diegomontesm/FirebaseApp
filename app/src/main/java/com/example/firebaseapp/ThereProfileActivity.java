package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postsRecyclerView;
    FirebaseAuth firebaseAuth;

    //views from xml
    ImageView avatarTv, coverTv;
    TextView nameTv, emailTv, phoneTv;

    //VARIABLES GLOBALES
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Perfil");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //INIT VIEWS
        postsRecyclerView = findViewById(R.id.recyclerview_post);
        firebaseAuth = FirebaseAuth.getInstance();

        //Inicializar vistas
        avatarTv = findViewById(R.id.avatarIv);
        coverTv = findViewById(R.id.coverId);
        nameTv = findViewById(R.id.NameTv);
        emailTv = findViewById(R.id.EmailTv);
        phoneTv = findViewById(R.id.PhoneTv);
        RevisarEstatusUsuario();

        //OBTEN UID DE LOS USUARIOS CLICKEADOS DE SUS POSTS
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Usuarios").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Revisar hasta obtener la info
                for(DataSnapshot ds :  dataSnapshot.getChildren()){
                    //OBTENER INFO
                    String name= "" + ds.child("nombre").getValue();
                    String email= "" + ds.child("email").getValue();
                    String phone= "" + ds.child("telefono").getValue();
                    String image= "" + ds.child("imagen").getValue();
                    String cover = "" +  ds.child("cover").getValue();

                    //Establecer info
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{
                        //SI LA IMAGEN ES RECIBIDA LUEGO ESTABLECE
                        Picasso.get().load(image).into(avatarTv);
                        //
                    }catch (Exception e){
                        //SI HAY ALGUNA EXCEPCION MIENTRAS OBTENEMOS LA IMG ESTABLECEMOS DEFAULT
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarTv);
                    }

                    try{
                        //SI LA IMAGEN ES RECIBIDA LUEGO ESTABLECE
                        Picasso.get().load(cover).into(coverTv);
                        //
                    }catch (Exception e){
                        //SI HAY ALGUNA EXCEPCION MIENTRAS OBTENEMOS LA IMG ESTABLECEMOS DEFAULT
                        // Picasso.get().load(R.drawable.ic_default_img_white).into(coverTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        --------------------------------------------------------------------------------
         */

        postList = new ArrayList<>();

        RevisarEstatusUsuario();
        cargarSusPosts();

    }

    private void cargarSusPosts() {
        //LinearLayout para RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //Muestra los nuevos posts primero,
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        //Establece este layout al reccyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //Inicia la lista de posts
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
        //Consulta para cargar posts
        Query query = dbRef.orderByChild("uid").equalTo(uid);
        //OBTEN TODA LA INFO
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost misPosts = ds.getValue(ModelPost.class);
                    //AGREGAR A LA LISTA
                    postList.add(misPosts);
                    //ADAPTADOR
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //ESTABLECE ESTE ADAPTADOR AL RECYCLERVIEW
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarSusPosts(final String buscarConsulta){
        //LinearLayout para RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //Muestra los nuevos posts primero,
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        //Establece este layout al reccyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //Inicia la lista de posts
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
        //Consulta para cargar posts
        Query query = dbRef.orderByChild("uid").equalTo(uid);
        //OBTEN TODA LA INFO
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost misPosts = ds.getValue(ModelPost.class);

                    if(misPosts.getpTitulo().toLowerCase().contains(buscarConsulta.toLowerCase()) ||
                            misPosts.getpDescripcion().toLowerCase().contains(buscarConsulta.toLowerCase())){
                        //AGREGAR A LA LISTA
                        postList.add(misPosts);
                    }

                    //AGREGAR A LA LISTA
                    postList.add(misPosts);
                    //ADAPTADOR
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //ESTABLECE ESTE ADAPTADOR AL RECYCLERVIEW
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void RevisarEstatusUsuario(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //VALIDACION
        if(user != null){
            //user is signed and stay here
            // Establece emaildel logueado
            // mProfileTv.setText(user.getEmail());

        }else{
            //user isnt signed fo to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);//OCULTAR DESDE ESTA ACTIVITY

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchveiw
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //LLAMADO CUANDO PRESIONA EL BOTON DE BUSQUEDA
                if(!TextUtils.isEmpty(query)){
                    //Busca
                    buscarSusPosts(query);
                }else{
                    cargarSusPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //LLAMADO CUANDO SE PRESIONA CUALQUIER LETRA
                //LLAMADO CUANDO PRESIONA EL BOTON DE BUSQUEDA
                if(!TextUtils.isEmpty(newText)){
                    //Busca
                    buscarSusPosts(newText);
                }else{
                    cargarSusPosts();
                }

                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            RevisarEstatusUsuario();
        }

        return super.onOptionsItemSelected(item);
    }
}