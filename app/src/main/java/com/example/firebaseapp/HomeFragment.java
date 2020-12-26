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
import android.widget.Adapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
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
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {


    //FIREBASE AUTENTICACION
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

   /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    } */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        //INICIALIZAR
        firebaseAuth = FirebaseAuth.getInstance();

        //Recycler view y sus propiedades
        recyclerView = view.findViewById(R.id.postRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //Muestra los nuevos posts primero
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //Establece el layout al recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //Inicializat Post List
        postList = new ArrayList<>();
        cargarPosts();
        return view;
    }

    private void cargarPosts() {
        //Ruta de todos los posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //Obten toda la informacion desde la referencia
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    //Adaptador
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //Establece el adaptador del recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
            //En caso de error
                Toast.makeText(getActivity(), ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarPosts(final String searchQuery){
        //Ruta de todos los posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //Obten toda la informacion desde la referencia
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    if(modelPost.getpTitulo().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescripcion().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }


                    //Adaptador
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //Establece el adaptador del recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
                //En caso de error
                Toast.makeText(getActivity(), ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();
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

        //SearchView para buscar posts por TITULO o descripcion
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Llamado cuando el usuario presiona el boton de busqueda
                if(!TextUtils.isEmpty(query)){
                    buscarPosts(query);
                }else{
                    cargarPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Llamado cuando el usuario presiona cualquier letra
                if(!TextUtils.isEmpty(newText)){
                    buscarPosts(newText);
                }else{
                    cargarPosts();
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