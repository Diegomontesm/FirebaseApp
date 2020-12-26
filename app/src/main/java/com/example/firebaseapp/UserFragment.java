package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.firebaseapp.adapters.AdapterUsers;
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
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    //FIREBASE AUTENTICACION
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
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
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //INICIALIZAR
        firebaseAuth = FirebaseAuth.getInstance();

        //INIT RECYVLERVIEW
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //ESTABLECE SUS PROPIEDADES
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //INIT USER LIST
        usersList = new ArrayList<>();

        //Obtener todos los usuarios
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //OBTENER EL USUARIO ACTUAL
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //OBTEN LA RUTA DE LA DATABASE  LLAMADA USUARIOS CONTENIENDO LA INFO DE LOS USERS
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Usuarios");
        //OBTENER TODA LA INFO DESDE LA RUTA
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //OBTEN TODOS LOS USUARIOS EXCEPTO EL QUE ESTA LOGEADO ACTUALEMENTE
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);

                     //Adaptador
                        adapterUsers = new AdapterUsers(getActivity(), usersList);

                     //ESTABLECE ADPATADOR
                        recyclerView.setAdapter(adapterUsers);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(final String query) {
        //OBTENER EL USUARIO ACTUAL
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //OBTEN LA RUTA DE LA DATABASE  LLAMADA USUARIOS CONTENIENDO LA INFO DE LOS USERS
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Usuarios");
        //OBTENER TODA LA INFO DESDE LA RUTA
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    /*  CONDICIONES PARA LA BUSQUEDA
                    1- El usuario NO ES el usuario actualmente logueado
                    2.- El nombre de usuario o email contiene texto ingresado en el SearchView (no sensible a MAYUSCULAS o minusculas)
                     */

                    //OBTEN TODOS LOS USUARIOS !!BUSCADOS!! EXCEPTO EL QUE ESTA LOGEADO ACTUALEMENTE
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        if(modelUsers.getNombre().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);
                        }

                        //Adaptador
                        adapterUsers = new AdapterUsers(getActivity(), usersList);
                        //ACTUALIZAR ADAPTER
                        adapterUsers.notifyDataSetChanged();
                        //ESTABLECE ADPATADOR
                        recyclerView.setAdapter(adapterUsers);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Inflate options menu */
    /* Inflar y adjuntar vistas */

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

        //Search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //LLAMADO CUANDO UN USUARIO PRESIONA EL BOTON DE BUSQUEDA DESDE EL TECLADO
                //SI LA CONSULTA NO ESTA VACIA ENTONCES BUSCA
                if(!TextUtils.isEmpty(s.trim())){
                    //BUSCA TEXTO QUE CONTIENE
                    searchUsers(s);
                }else{
                //BUSCA CONSULTA VACIA, MUESTRA TODOS LOS USUARIOS
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //LLAMADO CUANDO EL USUARIO PRESIONA CUALQUIER LETRA
                //LLAMADO CUANDO UN USUARIO PRESIONA EL BOTON DE BUSQUEDA DESDE EL TECLADO
                //SI LA CONSULTA NO ESTA VACIA ENTONCES BUSCA
                if(!TextUtils.isEmpty(s.trim())){
                    //BUSCA TEXTO QUE CONTIENE
                    searchUsers(s);
                }else{
                    //BUSCA CONSULTA VACIA, MUESTRA TODOS LOS USUARIOS
                    getAllUsers();
                }
                return false;
            }
        });



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