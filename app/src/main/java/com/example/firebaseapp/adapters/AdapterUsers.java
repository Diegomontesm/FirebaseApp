package com.example.firebaseapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.ChatActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.ThereProfileActivity;
import com.example.firebaseapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUsers> usersList;

    //CONSTRUCTOR

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //Inflate Layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //OBTENER DATOS
        final String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImagen();
        String userName = usersList.get(i).getNombre();
        final String userEmail = usersList.get(i).getEmail();

        //ESTABLECER INFO
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img_darkblue).into(myHolder.mAvatarIv);

        }catch(Exception e){

        }

        //MANEJADOR ITEM CLICK
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MUESTRA DIALOG
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Perfil", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //El perfil ha sido clickeado
                            /* Click para ir a ThereProfileActivity con uid, este uid es del usuario que ha sido
                             clickeado, EL CUAL SERA USADO PARA MOSTRAR POSTS ESPECIFICOS
                              */
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        }
                        if(which == 1){
                            // CHAT ha sido clickeado
                             /* Click del usuario desde la lista de usuarios para empezar a conversar
                            Empieza activyty poniendo UID o reciviendo
                            Usamos el UID para identificar los usuarios que van a chatear
                            */
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUID", hisUID);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{


        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //INICIALIZAR
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.NameTv);
            mEmailTv = itemView.findViewById(R.id.EmailTv);

        }
    }



}
