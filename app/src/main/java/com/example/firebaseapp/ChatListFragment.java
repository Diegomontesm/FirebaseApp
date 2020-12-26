package com.example.firebaseapp;

import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.firebaseapp.adapters.AdapterChatlist;
import com.example.firebaseapp.models.ModelChat;
import com.example.firebaseapp.models.ModelChatList;
import com.example.firebaseapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {
    //FIREBASE AUTH
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatListList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatListFragment newInstance(String param1, String param2) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        //INICIALIZAR
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatListList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChatList chatList = ds.getValue(ModelChatList.class);
                    chatListList.add(chatList);
                }
                cargarChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void cargarChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Usuarios");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers user = ds.getValue(ModelUsers.class);
                    for(ModelChatList chatList : chatListList){
                        if(user.getUid() != null && user.getUid().equals(chatList.getId())){
                            usersList.add(user);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist = new AdapterChatlist(getContext(), usersList);
                    //SET ADAPTER
                    recyclerView.setAdapter(adapterChatlist);
                    //SET LAST MESSAGE
                    for(int i=0; i<usersList.size(); i++){
                        ultimoMensaje(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ultimoMensaje(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               String ElultimoMensaje = "default";
               for(DataSnapshot ds: dataSnapshot.getChildren()){
                   ModelChat chat = ds.getValue(ModelChat.class);
                   if(chat==null){
                       continue;
                   }
                   String sender = chat.getEnviador();
                   String receiver = chat.getReceptor();
                   if(sender==null || receiver==null){
                       continue;
                   }
                   if(chat.getReceptor().equals(currentUser.getUid()) &&
                        chat.getEnviador().equals(userId) ||
                        chat.getReceptor().equals(userId) &&
                        chat.getEnviador().equals(currentUser.getUid())){
                       ElultimoMensaje = chat.getMensaje();
                   }
               }
               adapterChatlist.setLastMessageMap(userId, ElultimoMensaje);
               adapterChatlist.notifyDataSetChanged();
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
        }else{
            //user isnt signed fo to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);//PARA MOSTRAR EL MENU DE OPCIONES EN ESTE FRAGMENTO
        super.onCreate(savedInstanceState);
    }
    /* inflate opciones menu */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Inflating enu
        inflater.inflate(R.menu.menu_main, menu);

        //OCULTAR ADDPOST DESDE ESTE FRAGMENTO
        menu.findItem(R.id.action_add_post).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }


    /* manejador menu item clicks */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //obtener el id del item
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            RevisarEstatusUsuario();
        }

        //POST
        if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}