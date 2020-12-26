package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.adapters.AdapterChat;
import com.example.firebaseapp.models.ModelChat;
import com.example.firebaseapp.models.ModelUsers;
import com.example.firebaseapp.notifications.APIService;
import com.example.firebaseapp.notifications.Client;
import com.example.firebaseapp.notifications.Data;
import com.example.firebaseapp.notifications.Response;
import com.example.firebaseapp.notifications.Sender;
import com.example.firebaseapp.notifications.Token;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;


public class ChatActivity extends AppCompatActivity {
    //VISTAS DESDE XML
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBTN;

    //FIREBASE AUTENTICACION
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;

    //Para checar si se ha enviado o no
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUID;
    String myUID;
    String hisImage;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //INICIALIZAR VARIABLES
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.NameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBTN = findViewById(R.id.sendBTN);

        //LAYOUT LINEAR LAYOUT PARA EL RECYVLEVIEW
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recycleview porpiedades
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //CREAR SERVICIO API
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        /* Haciendo click desde la lista de los usuarios hemos pasado el UID usando intent
            Para tener ese UID aqui para tener la foto de perfil nombre e iniciar chat con ese user
         */
        Intent intent = getIntent();
        hisUID = intent.getStringExtra("hisUID");

        //Firebase authentication and instance
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef=firebaseDatabase.getReference("Usuarios");

        //Buscar usuarios para tener su info
        Query userquery = userDbRef.orderByChild("uid").equalTo(hisUID);

        //Tener foto de perfil y nombre
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //Cehcar hasta que la info requerida sea recibida
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //OBTEN LA INFO
                    String name = ""+ds.child("nombre").getValue();
                    hisImage = ""+ds.child("imagen").getValue();
                    //ESCRIBIENDO
                    String escribiendoStatus =  ""+ds.child("escribiendoA").getValue();
                    //REVISAR ESTATUS ESCRIBIENDO
                    if(escribiendoStatus.equals(myUID)){
                        userStatusTv.setText("Escribiendo...");
                    }else{
                        //OBTEN EL VALOR DEL ONLINESTATUS
                        String onlineStatus = ""+ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }else{
                            //Convierte timestamp a la hora correcta
                            //CONVERSION TIMESTAMP TO DD/MM/AAAA hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            userStatusTv.setText("Última conexión a las:" + dateTime);

                        }
                    }
                    //ESTABLECE LA INFO
                    nameTv.setText(name);
                    try{
                        //La imagen es recibida , ponla en el toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv);
                    }catch (Exception e){
                        //La imagen no fue recibida, obten el error y muestralo, pon la img por defecto
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Boton para enviar mensaje
        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NOTIFICAR
                notify = true;
                //Obtener el texto del edittext
                String message = messageEt.getText().toString().trim();
                //Revisar si el texto tiene contenido o no
                if(TextUtils.isEmpty(message)){
                    //TEXTTO VACIO
                    Toast.makeText(ChatActivity.this, "No puedes enviar mensajes vacíos", Toast.LENGTH_SHORT).show();
                }else{
                    //El texto tiene contenido
                    enviarMensaje(message);
                }
                //Deja limpio el edittext despues de enviar el mensaje
                messageEt.setText("");
            }
        });
        //Revisar editText listener de cambios
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    RevisarEstatusEscribiendo("nadie");
                }else{
                    RevisarEstatusEscribiendo(hisUID);//UID del que lo recibe
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        leerMensajes();
        verMensaje();
    }

    private void verMensaje() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    //LINEA VALIDACION NULL
                    if( chat != null){
                        if(chat.getReceptor().equals(myUID) && chat.getEnviador().equals(hisUID)){
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("Visto", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void leerMensajes() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                chatList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);

                        if(chat.getReceptor().equals(myUID) && chat.getEnviador().equals(hisUID) ||
                                chat.getReceptor().equals(hisUID) && chat.getEnviador().equals(myUID)){
                            chatList.add(chat);
                        }


                    //ADAPTADOR
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //ESTABLECE ADAPTADOR AL RECYCLEVIEW
                    recyclerView.setAdapter(adapterChat);
                    //ENCONTRE QUE ESTA LINEA SOLUCIONA PROBLEMAS
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void enviarMensaje(final String message) {
        /* CHATNODE sera creado y contendra todos los chats
            Siempre que un usuario envie un mensaje se creara un nuevo hijo en el nodo de "chats" y ese hijo contenra los siguientes valores
            "Sender" o el que envia : UID del sender
            "Receiver" o receptor: UID del receptor
            "Mensaje": cuerpo del mensaje
         */
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("enviador", myUID);
        hashMap.put("receptor", hisUID);
        hashMap.put("mensaje", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("fueVisto", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        String msg = message;
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Usuarios").child(myUID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers users = dataSnapshot.getValue(ModelUsers.class);
                if(notify){
                    EnviarNotificacion(hisUID, users.getNombre(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //CREATE CHATLIST NODO/CHILD    IN FIREBASE DATABASE
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUID)
                .child(hisUID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUID)
                .child(myUID);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void EnviarNotificacion(final String hisUID, final String nombre, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUID, nombre+":"+message, "Nuevo mensaje", hisUID, R.drawable.ic_default_img_darkblue);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
            myUID = user.getUid(); //Actualmente firmado en userUID
        }else{
            //user isnt signed fo to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void RevisarEstatusOnline(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(myUID);
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("onlineStatus", status);
        //ACTUALIZAR VALOR DE ONLINE STATUS DEL USUARIO ACTUAL
        dbRef.updateChildren(hashMap);

    }
    private void RevisarEstatusEscribiendo(String escribiendo){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(myUID);
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("escribiendoA", escribiendo);
        //ACTUALIZAR VALOR DE ONLINE STATUS DEL USUARIO ACTUAL
        dbRef.updateChildren(hashMap);

    }

    @Override
    protected void onStart() {
        RevisarEstatusUsuario();
        //ESTABLECER ONLINE
        RevisarEstatusOnline("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //GET TIMESTAMP
        String timestamp = String.valueOf(System.currentTimeMillis());
        //ESTABLECER OFFLINE con hora
        RevisarEstatusOnline(timestamp);
        userRefForSeen.removeEventListener(seenListener);
        //STATUS ESCRIBIENDO
        RevisarEstatusEscribiendo("nadie");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Ocultar la vista de busqueda, agregar POSTS,no lo neccesitaremos aqui
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener el id del item
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            RevisarEstatusUsuario();
        }
        return super.onOptionsItemSelected(item);
    }
}