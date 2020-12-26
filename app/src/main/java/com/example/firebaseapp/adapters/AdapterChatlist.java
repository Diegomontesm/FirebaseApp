package com.example.firebaseapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.ChatActivity;
import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder>{
    Context context;
    List<ModelUsers> userList; //Get user info
    private HashMap<String, String> lastMessageMap;


    public AdapterChatlist(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layyotrow_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //Obten la info
        String hisUID = userList.get(i).getUid();
        String userImage = userList.get(i).getImagen();
        String userName = userList.get(i).getNombre();
        String lastMessage = lastMessageMap.get(hisUID);
        //ESTABLECE LA INFO
        myHolder.nameTv.setText(userName);
        if(lastMessage == null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }else{
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_darkblue).into(myHolder.profileIv);
        }catch(Exception e ){
            Picasso.get().load(R.drawable.ic_default_img_darkblue).into(myHolder.profileIv);
        }

        //Establece el status de los otros usuarios en el chatlist
        if(userList.get(i).getOnlineStatus().equals("online")){
            //ONLINE
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else{
            //OFFLINE
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        //MANEJADOR DEL CLICK EN CHATLIST
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EMPIEZA EL CHAT ACTIVITY CON EL USUARIO
                Intent  intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID", hisUID);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();//Tamaño de la lista
    }

    class MyHolder extends RecyclerView.ViewHolder{
    //Vistas de row_chatlist.xml
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //INIT VIEWS
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
