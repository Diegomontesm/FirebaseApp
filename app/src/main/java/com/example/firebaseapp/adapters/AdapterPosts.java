package com.example.firebaseapp.adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.AddPostActivity;
import com.example.firebaseapp.PostDetailActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.ThereProfileActivity;
import com.example.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUID;

    private DatabaseReference likesRef; //ForLikes Database Node
    private DatabaseReference postsRef; //Referencia del post

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Inflate layout row.post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //Obtener informacion
        String uid = postList.get(i).getUid();
        String uEmail = postList.get(i).getuEmail();
        String uNombre = postList.get(i).getuNombre();
        String uDp = postList.get(i).getuDp();
        String pId = postList.get(i).getpId();
        String pTitulo = postList.get(i).getpTitulo();
        String pDescripcion = postList.get(i).getpDescripcion();
        String pImagen = postList.get(i).getpImagen();
        String pTimeStamp = postList.get(i).getpHora();
        String pLikes = postList.get(i).getpLikes(); //Contiene el numero total de likes del post
        String pComentarios = postList.get(i).getpComentarios(); //Contiene el numero total de likes del post

        //Convertir timestamp a dd/mm/aaaa hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pHora = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        //Establecer informacion
        myHolder.uNombreTv.setText(uNombre);
        myHolder.pHoraTv.setText(pHora);
        myHolder.pTituloTv.setText(pTitulo);
        myHolder.pDescripcionTv.setText(pDescripcion);
        myHolder.pLikesTv.setText(pLikes + "Likes");
        myHolder.pComentariosTv.setText(pComentarios + "Comentarios");
        //Establece los likes para cada post
        setLikes(myHolder, pId);

        //Establece Usuario dp
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img_darkblue).into(myHolder.uPictureIv);
        }catch (Exception e){

        }

        //Establece la imagen del post
        //Si no hay imagen , pImagen.equals("noImagen")
        if(pImagen.equals("noImagen")){
            //Esconde imageview
            myHolder.pImageIv.setVisibility(View.GONE);
        }else{
            //Muestra imageview
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImagen).into(myHolder.pImageIv);
            }catch (Exception e){

            }
        }


        try{
            Picasso.get().load(pImagen).into(myHolder.pImageIv);
        }catch (Exception e){

        }

        //Manejador de Boton Click
        myHolder.masBTN.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                mostrarMasOpciones(myHolder.masBTN, uid, myUID, pId, pImagen);
            }
        });
        //Manejador de Boton LIKE
        myHolder.LikeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obten el numero total de likes del post, si el usuario actual no le ha dado like antes
                //incrementa el valor a 1, de otra manera, decrementa a 1
                final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;
                //Obten el id del post clickeado
                String postIde = postList.get(i).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike) {
                            if(dataSnapshot.child(postIde).hasChild(myUID)){
                                //Ya le dio like, entonces quita el like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUID).removeValue();
                                mProcessLike = false;
                            }
                            else{
                                //No le ha dado like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUID).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        //Manejador de Boton COMENTAR
        myHolder.comentarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Empieza PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //Obtendremos detalles del post usando este id (ID del post clickeado)
                context.startActivity(intent);
            }
        });
        //Manejador de Boton COMPARTIR
        myHolder.compartirBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lo implemetnarmos despues
                Toast.makeText(context, "COMPARTIR", Toast.LENGTH_SHORT).show();
            }
        });

        //Manejador de profile Layout
        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Click para ir a ThereProfileActivity con uid, este uid es del usuario que ha sido
                clickeado, EL CUAL SERA USADO PARA MOSTRAR POSTS ESPECIFICOS
                 */
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.child(postKey).hasChild(myUID)){
                //Usuario le ha dado like al post
                /* Para indicar que el post tiene likes por este(UsuarioLogueado)
                    cambia el drawable icono de like de la izquierda
                    cambia el texto de "Like" a "Liked"
                 */
                holder.LikeBTN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
                holder.LikeBTN.setText("Liked");
            }else{
                //Usuario no le ha dado like al post
                //Para indicar que el post NO tiene likes por este(UsuarioLogueado)
                //Cambia el icono de la izquierda del like
                holder.LikeBTN.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0,0,0);
                holder.LikeBTN.setText("Like");
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void mostrarMasOpciones(ImageButton masBTN, String uid, String myUID, final String pId,final String pImagen) {
        //Creamos un popUp menu teniendo la opcion de Borrar, añadiremos mas opciones luego
        PopupMenu popupMenu = new PopupMenu(context, masBTN, Gravity.END);

        //Muestra la opcion de borrar solo en los post del usuario actualmente loagueado
        if(uid.equals(myUID)){
            //AQUI se AGREGAN items al menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Borrar");//[1 BTN BORRAR]
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Editar");//[2 BTN EDITAR]
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Ver detalles");

            //ITEM CLICKED LISTENER
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if(id == 0){
                        //BORRAR HA SIDO CLICKEADO
                        empiezaBorrar(pId, pImagen);
                    }
                    else if(id == 1){
                        //EDITAR HA SIDO CLICKEADO
                        //EMPIEZA "STARTPOSTACTIVITY CON LA LLAVE "editPost" y el id del post seleccionado
                        Intent intent = new Intent(context, AddPostActivity.class);
                        intent.putExtra("key", "editarPost");
                        intent.putExtra("editarPostId", pId);
                        context.startActivity(intent);
                    }
                    else if(id == 2){
                        //Empieza PostDetailActivity
                        Intent intent = new Intent(context, PostDetailActivity.class);
                        intent.putExtra("postId", pId); //Obtendremos detalles del post usando este id (ID del post clickeado)
                        context.startActivity(intent);
                    }
                    return false;
            }
        });
        //Muestra el menu
        popupMenu.show();
    }

    private void empiezaBorrar(String pId, String pImagen){
        //POST PUEDEN TENER O NO IMAGEN
        if(pImagen.equals("SinImagen")){
            //Post no tiene imagen
            borrarSinImagen(pId);
        }else{
            //Post con imagen
            borrarConImagen(pId, pImagen);
        }
    }

    private void borrarConImagen(String pId, String pImagen) {
        //ProgressBar
        final ProgressDialog pd = new ProgressDialog(context);
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
                                .orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();//BORRA LOS VALORES DE FIREBASE DONDE pId coincide
                                }
                                //BORRADO
                                Toast.makeText(context, "¡Post borrado exitosamente!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void borrarSinImagen(String pId) {
        //ProgressBar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Borrando...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();//BORRA LOS VALORES DE FIREBASE DONDE pId coincide
                }
                //BORRADO
                Toast.makeText(context, "¡Post borrado exitosamente!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //View holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //VISTAS DESDE Row_post.xml
        ImageView uPictureIv, pImageIv;
        TextView uNombreTv, pHoraTv, pTituloTv, pDescripcionTv, pLikesTv, pComentariosTv;
        ImageButton masBTN;
        Button LikeBTN, comentarBTN, compartirBTN;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //INICIALIZAR LAS VISTAS
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNombreTv = itemView.findViewById(R.id.uNameTv);
            pHoraTv = itemView.findViewById(R.id.pTimeTv);
            pTituloTv = itemView.findViewById(R.id.pTitleTv);
            pDescripcionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pComentariosTv = itemView.findViewById(R.id.pComentariosTv);
            masBTN = itemView.findViewById(R.id.moreBTN);
            LikeBTN = itemView.findViewById(R.id.likeBTN);
            comentarBTN = itemView.findViewById(R.id.comentarBTN);
            compartirBTN = itemView.findViewById(R.id.compartirBTN);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
