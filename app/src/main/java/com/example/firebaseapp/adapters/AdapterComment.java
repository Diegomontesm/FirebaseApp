package com.example.firebaseapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder>{

    Context context;
    List<ModelComment> commentList;
    String myUID, postId;

    public AdapterComment(Context context, List<ModelComment> commentList, String myUID, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUID = myUID;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Enlazar row_comment.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //Obten la info
        String uid = commentList.get(i).getUid();
        String nombre = commentList.get(i).getuNombre();
        String email = commentList.get(i).getuEmail();
        String imagen = commentList.get(i).getuDp();
        String cid = commentList.get(i).getcId();
        String comentario = commentList.get(i).getComentario();
        String timestamp = commentList.get(i).getTimestamp();

        //Convertir timestamp a dd/mm/aaaa hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pHora = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        //Establece la info
        myHolder.nombreTv.setText(nombre);
        myHolder.comentarioTv.setText(comentario);
        myHolder.timeTv.setText(pHora);

        //Establece dp del usuario
        try{
            Picasso.get().load(imagen).placeholder(R.drawable.ic_default_img_darkblue).into(myHolder.avatarIv);
        }catch (Exception e){

        }

        //Comment click listener
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Revisamos si este comentario es usado por el usuario actualmente logueado
                if(myUID.equals(uid)){
                    //My comment
                    //Show delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Borrar");
                    builder.setMessage("¿Estás seguro de que quieres borrar este comentario?");
                    builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Borrar comentario
                            borrarComentario(cid);
                        }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Descartar dialog
                            dialog.dismiss();
                        }
                    });
                    //Show dialog
                    builder.create().show();
                }
                else{
                    //No my comment
                    Toast.makeText(context, "No puedes borrar los comentarios de los demás", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void borrarComentario(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comentarios").child(cid).removeValue();//Borrar comentario

        //Ahora actualiza el contador de comentarios.
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comentarios = ""+ dataSnapshot.child("pComentarios").getValue();
                int nuevoComentarioVal = Integer.parseInt(comentarios) -1;
                ref.child("pComentarios").setValue(""+nuevoComentarioVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //Declara las vistas desde row_comments.xml
        ImageView avatarIv;
        TextView nombreTv, comentarioTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //Inicializar las vistas
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nombreTv = itemView.findViewById(R.id.nameTv);
            comentarioTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

}
