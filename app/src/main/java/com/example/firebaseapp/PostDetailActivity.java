package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.adapters.AdapterComment;
import com.example.firebaseapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    // Para obtener detalles del usuario y del post
    String hisUID, myUID, myEmail, myNombre, myDp,
            postId, pLikes, hisDp, hisNombre, pImagen;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //ProgressBar
    ProgressDialog pd;

    //Vistas
    ImageView uPictureIv, pImagenIv;
    TextView uNombreTv, pHoraTv, pTituloTv, pDescripcionTv, pLikesTv, pComentariosTv;
    ImageView masBTN;
    Button likeBTN, compartirBTN;
    LinearLayout perfilLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComment adapterComment;

    //Vista de Añadir comentarios
    EditText comentarEt;
    ImageButton enviarBTN;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //Action bar y sus propiedades
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Detalles del post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Inicializar vistas
        uPictureIv = findViewById(R.id.uPictureIv);
        pImagenIv = findViewById(R.id.pImageIv);
        uNombreTv = findViewById(R.id.uNameTv);
        pHoraTv = findViewById(R.id.pTimeTv);
        pTituloTv = findViewById(R.id.pTitleTv);
        pDescripcionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pComentariosTv = findViewById(R.id.pComentariosTv);
        masBTN = findViewById(R.id.moreBTN);
        likeBTN = findViewById(R.id.likeBTN);
        compartirBTN = findViewById(R.id.compartirBTN);
        perfilLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        //Obtener ID del post usando intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        comentarEt = findViewById(R.id.comentarEt);
        enviarBTN = findViewById(R.id.enviarBTN);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        cargarPostInfo();
        RevisarEstatusUsuario();
        cargarInfoUsuario();

        //Establece los likes para cada post
        setLikes();

        //Establecer subtitulo al actionbar
        actionBar.setSubtitle("Entraste como: "+myEmail);

        cargarComentarios();

        //Envia comentarios button link
        enviarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postearComentario();
            }
        });

        //Manejador de clicks del LIKE BTN
        likeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        //Manejador del boton Mas
        masBTN.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                mostrarMasOpciones();
            }
        });
    }

    private void cargarComentarios() {
        //Layout Linear para el RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //Establece el layout al recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //Iniciarlizar
        commentList = new ArrayList<>();

        //Ruta del post, para obtener sus comentarios
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comentarios");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //Pasamos el myUID y postUID, como parametro al contrsuctor(BORRAR)

                    //SET UP ADAPTER
                    adapterComment = new AdapterComment(getApplicationContext(), commentList, myUID, postId);
                    //ESTABLECE ADAPTER
                    recyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void mostrarMasOpciones() {
        //Creamos un popUp menu teniendo la opcion de Borrar, añadiremos mas opciones luego
        PopupMenu popupMenu = new PopupMenu(this, masBTN, Gravity.END);

        //Muestra la opcion de borrar solo en los post del usuario actualmente loagueado
        if(hisUID.equals(myUID)){
            //AQUI se AGREGAN items al menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Borrar");//[1 BTN BORRAR]
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar");//[2 BTN EDITAR]
        }


        //ITEM CLICKED LISTENER
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    //BORRAR HA SIDO CLICKEADO
                    empiezaBorrar();
                }
                else if(id == 1){
                    //EDITAR HA SIDO CLICKEADO
                    //EMPIEZA "STARTPOSTACTIVITY CON LA LLAVE "editPost" y el id del post seleccionado
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editarPost");
                    intent.putExtra("editarPostId", postId);
                    startActivity(intent);
                }

                return false;
            }
        });
        //Muestra el menu
        popupMenu.show();
    }

    private void empiezaBorrar() {
        //POST PUEDEN TENER O NO IMAGEN
        if(pImagen.equals("SinImagen")){
            //Post no tiene imagen
            borrarSinImagen();
        }else{
            //Post con imagen
            borrarConImagen();
        }
    }

    private void borrarConImagen() {
        //ProgressBar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Borrando...");

        /* Pasos.
        1) Borrar imagen usando URL
        2) Borrar desde la base de datos de FireBase usando postId
         */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImagen);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Imagen borrada
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts")
                                .orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();//BORRA LOS VALORES DE FIREBASE DONDE pId coincide
                                }
                                //BORRADO
                                Toast.makeText(PostDetailActivity.this, "¡Post borrado exitosamente!", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Error, no podemos continuar
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void borrarSinImagen() {
        //ProgressBar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Borrando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();//BORRA LOS VALORES DE FIREBASE DONDE pId coincide
                }
                //BORRADO
                Toast.makeText(PostDetailActivity.this, "¡Post borrado exitosamente!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        //Cuando los detalles del post se cargan , revisamos si el usuario actual le ha dado like o no
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUID)){
                    //Usuario le ha dado like al post
                /* Para indicar que el post tiene likes por este(UsuarioLogueado)
                    cambia el drawable icono de like de la izquierda
                    cambia el texto de "Like" a "Liked"
                 */
                    likeBTN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
                    likeBTN.setText("Liked");
                }else{
                    //Usuario no le ha dado like al post
                    //Para indicar que el post NO tiene likes por este(UsuarioLogueado)
                    //Cambia el icono de la izquierda del like
                    likeBTN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
                    likeBTN.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        //bten el numero total de likes del post, si el usuario actual no le ha dado like antes
        //incrementa el valor a 1, de otra manera, decrementa a 1
        mProcessLike = true;
        //Obten el id del post clickeado
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLike) {
                    if(dataSnapshot.child(postId).hasChild(myUID)){
                        //Ya le dio like, entonces quita el like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUID).removeValue();
                        mProcessLike = false;

                    }

                    else{
                        //No le ha dado like
                        postsRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                        likesRef.child(postId).child(myUID).setValue("Liked");
                        mProcessLike = false;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postearComentario() {
        pd = new ProgressDialog(this);
        pd.setMessage("Añadiendo comentario...");

        //Obten data del edit text de comentario
        String comentario = comentarEt.getText().toString().trim();
        //Valida
        if(TextUtils.isEmpty(comentario)){
            //No se han ingresado valores
            Toast.makeText(this, "Comentario vacío...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //Cada post tendrá un hijo "Comentarios" y contendra los comentarios de cada post.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comentarios");

        HashMap<String, Object> hashMap = new HashMap<>();
        //Pon la info en el hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comentario", comentario);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUID);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uNombre", myNombre);

        //Pon esta info en la DB
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Comentario agregado
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comentario agregado...", Toast.LENGTH_SHORT).show();
                        comentarEt.setText("");
                        actualizarContadorComentario();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                 @Override
                    public void onFailure(@NonNull Exception e) {
                        //Error al añadir comentario
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarContadorComentario() {
        //Cuandosea que el usuario añada un comentario incrementa el contador tal como fue hecho el contador de likes
        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessComment){
                    String comentarios = ""+ dataSnapshot.child("pComentarios").getValue();
                    int nuevoComentarioVal = Integer.parseInt(comentarios) + 1;
                    ref.child("pComentarios").setValue(""+nuevoComentarioVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cargarInfoUsuario() {
        //Obten info del usuario actual
        Query myRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        myRef.orderByChild("uid").equalTo(myUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myNombre = ""+ds.child("nombre").getValue();
                    myDp = ""+ds.child("imagen").getValue();

                    //Establece la info
                    try{
                        //Si la imagen es recibida entonces establecela
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img_darkblue).into(cAvatarIv);
                    }catch(Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_darkblue).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cargarPostInfo() {
        //Obtener del post usando el ID del post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //Nos mantenemos revisando los posts hasta obtener el que requerimos
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    //Obten la info
                    String pTitulo = ""+ds.child("pTitulo").getValue();
                    String pDescr = ""+ds.child("pDescripcion").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pHora").getValue();
                    pImagen = ""+ds.child("pImagen").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUID = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisNombre = ""+ds.child("uNombre").getValue();
                    String comentariosContador = ""+ds.child("pComentarios").getValue();

                    //Convertir timestamp a su formato
                    //Convertir timestamp a dd/mm/aaaa hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pHora = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

                    //Establece la info
                    pTituloTv.setText(pTitulo);
                    pDescripcionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + " Likes");
                    pHoraTv.setText(pHora);
                    pComentariosTv.setText(comentariosContador + " Comentarios");

                    uNombreTv.setText(hisNombre);

                    //Establece la imagen del usuarioq que ha posteado algo
                    //Establece la imagen del post
                    //Si no hay imagen , pImagen.equals("noImagen")
                    if(pImagen.equals("noImagen")){
                        //Esconde imageview
                        pImagenIv.setVisibility(View.GONE);
                    }else{
                        //Muestra imageview
                        pImagenIv.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImagen).into(pImagenIv);
                        }catch (Exception e){

                        }
                    }
                    //Establece la imagen del usuario en la seccion de comentarios
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img_darkblue).into(uPictureIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_darkblue).into(uPictureIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void RevisarEstatusUsuario(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if( user != null){
            //Usuario esta logueado
            myEmail = user.getEmail();
            myUID = user.getUid();
        }else{
            //Usuario no esta logueado, ve a MainActivity
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
        //Esconde algunos items del menu
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener el id del item
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            RevisarEstatusUsuario();
        }
        return super.onOptionsItemSelected(item);
    }
}