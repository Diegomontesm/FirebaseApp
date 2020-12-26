package com.example.firebaseapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat>chatList;
    String imageURL;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageURL) {
        this.context = context;
        this.chatList = chatList;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflate layouts : row_chat_left para el que recibe rigth para el que envia
        if(i==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
    //OBTEN DATA
        String message = chatList.get(i).getMensaje();
        String timestamp = chatList.get(i).getTimestamp();


        //CONVERSION TIMESTAMP TO DD/MM/AAAA hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        //Set data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try{
            Picasso.get().load(imageURL).into(myHolder.profileIv);
        }catch (Exception e){

        }

        //Click para mostrar DELETE DIALOG
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Muestra alerta de borrar mensaje para confirmar que desea eliminar
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Borrar");
                builder.setMessage("¿Estás seguro que quieres borrar este mensaje?");
                //Boton borrar
                builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrarMensaje(i);
                    }
                });
                //CANCELAR borrar desde la advertencia
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Descarta ventana de dialogo
                        dialog.dismiss();
                    }
                });
                //CREAR Y MOSTRAR DIALOG SHOW
                builder.create().show();
            }
        });

        //ESTABLECER STATUS DEL MENSAHE VISTO O LEIDO
        if(i==chatList.size()-1){
           if(chatList.get(i).isFueVisto()){
               myHolder.isSeenTv.setText("Visto");
           }else{
               myHolder.isSeenTv.setText("Entregado");
           }
        }else{
        myHolder.isSeenTv.setVisibility(View.GONE);
        }


    }

    private void borrarMensaje(int i) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /* LOGICA:
        1.- Obtiene el timestamp del mensaje seleccionado
        2.-Compara el timestamp del mensaje clickeado con todos los demas
        3.- Cuando los dos valores coincidan se borra ese mensaje
         */
        String msgTimeStamp = chatList.get(i).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    /* Si tu quieres permitir borrar soloe este mensaje luego comparar el valor del enviador
                    con el actual userUID, si tiene coincidencia significa que el mensaje del enviador es el que
                    estas tratando de borrar.
                     */
                 if(ds.child("enviador").getValue().equals(myUID)){
                    /* Podemos hacer una o dos cosas aqui.
                    1) Eliminar el mensaje desde el chat.
                    2) Establecer el valor del mensaje "Este mensaje ha sido eliminado..."
                    */
                     //[1] Eliminar el mensaje del chat.
                     // ds.getRef().removeValue();
                     //[2] Establecer el valor del mensaje "Este mensaje ha sido eliminado..."
                     HashMap<String, Object> hashMap = new HashMap<>();
                     hashMap.put("mensaje", "Este mensaje ha sido eliminado...");
                     ds.getRef().updateChildren(hashMap);
                     Toast.makeText(context, "El mensaje se eliminó", Toast.LENGTH_SHORT).show();
                 }else{
                    Toast.makeText(context, "Puedes borrar solo los mensajes que tu envías", Toast.LENGTH_SHORT).show();
                 }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //Obtener del usuario actualemente logueado
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getEnviador().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    //View holder class
    class MyHolder extends RecyclerView.ViewHolder{
        //VISTAS
        ImageView profileIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; //Para el click listener para borrar mensajes

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //Init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }

}
