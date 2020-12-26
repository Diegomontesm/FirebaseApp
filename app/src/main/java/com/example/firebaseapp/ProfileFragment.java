package com.example.firebaseapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.adapters.AdapterPosts;
import com.example.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //ALmacenamiento
    StorageReference storageReference;

    // Ruta donde las imagenes de foto de perfil o portada seran almacenadas
    String rutaAlmacenamiento = "Users_Profile_Cover_Imgs/";

    //views from xml
    ImageView avatarTv, coverTv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;

    //ProgressDialog
    ProgressDialog pd;

    //PERMISOS
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK__GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arreglos de permisos que son requeridos
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //URL de la imagen escogida
    Uri image_uri;

    //Para checar foto perfil o portada
    String perfil_O_Portada;

    public ProfileFragment() {
        // Required empty public constructor
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
       View view = inflater.inflate(R.layout.fragment_profile, container, false);


       //Init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Usuarios");
        storageReference = FirebaseStorage.getInstance().getReference(); //REFERENCIA DE ALMACENAMIENTO FIREBASE

        //Inicializar Arreglos de permisos
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Inicializar vistas
        avatarTv = view.findViewById(R.id.avatarIv);
        coverTv = view.findViewById(R.id.coverId);
        nameTv = view.findViewById(R.id.NameTv);
        emailTv = view.findViewById(R.id.EmailTv);
        phoneTv = view.findViewById(R.id.PhoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerView = view.findViewById(R.id.recyclerview_post);

        //Init Progress Dialog
        pd = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Revisar hasta obtener la info
                for(DataSnapshot ds :  dataSnapshot.getChildren()){
                    //OBTENER INFO
                    String name= "" + ds.child("nombre").getValue();
                    String email= "" + ds.child("email").getValue();
                    String phone= "" + ds.child("telefono").getValue();
                    String image= "" + ds.child("imagen").getValue();
                    String cover = "" +  ds.child("cover").getValue();

                    //Establecer info
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{
                        //SI LA IMAGEN ES RECIBIDA LUEGO ESTABLECE
                        Picasso.get().load(image).into(avatarTv);
                        //
                    }catch (Exception e){
                        //SI HAY ALGUNA EXCEPCION MIENTRAS OBTENEMOS LA IMG ESTABLECEMOS DEFAULT
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarTv);
                    }

                    try{
                        //SI LA IMAGEN ES RECIBIDA LUEGO ESTABLECE
                        Picasso.get().load(cover).into(coverTv);
                        //
                    }catch (Exception e){
                        //SI HAY ALGUNA EXCEPCION MIENTRAS OBTENEMOS LA IMG ESTABLECEMOS DEFAULT
                        // Picasso.get().load(R.drawable.ic_default_img_white).into(coverTv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //FAB Floating Action Bar BUTTON CLICK
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();
        RevisarEstatusUsuario();
        cargarMisPosts();

        return view;
    }

    private void cargarMisPosts() {
        //LinearLayout para RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //Muestra los nuevos posts primero,
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        //Establece este layout al reccyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //Inicia la lista de posts
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
        //Consulta para cargar posts
        Query query = dbRef.orderByChild("uid").equalTo(uid);
        //OBTEN TODA LA INFO
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost misPosts = ds.getValue(ModelPost.class);
                    //AGREGAR A LA LISTA
                    postList.add(misPosts);
                    //ADAPTADOR
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //ESTABLECE ESTE ADAPTADOR AL RECYCLERVIEW
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarMisPosts(String buscarConsulta) {
        //LinearLayout para RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //Muestra los nuevos posts primero,
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        //Establece este layout al reccyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //Inicia la lista de posts
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
        //Consulta para cargar posts
        Query query = dbRef.orderByChild("uid").equalTo(uid);
        //OBTEN TODA LA INFO
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost misPosts = ds.getValue(ModelPost.class);

                    if(misPosts.getpTitulo().toLowerCase().contains(buscarConsulta.toLowerCase()) ||
                    misPosts.getpDescripcion().toLowerCase().contains(buscarConsulta.toLowerCase())){
                        //AGREGAR A LA LISTA
                        postList.add(misPosts);
                    }

                    //AGREGAR A LA LISTA
                    postList.add(misPosts);
                    //ADAPTADOR
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //ESTABLECE ESTE ADAPTADOR AL RECYCLERVIEW
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean revisarPermisosAlmacenamiento(){
        //REVISAR SI LOS PERMISOS ESTAN ACTIVADOS O NO
        //RETURN TRUE SI SI LO ESTAN
        //FALSE SI NO
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //Peticion de tiempo de ejecucion
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);

    }
    private boolean revisarPermisosCamara(){
        //REVISAR SI LOS PERMISOS ESTAN ACTIVADOS O NO
        //RETURN TRUE SI SI LO ESTAN
        //FALSE SI NO
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //Peticion de tiempo de ejecucion
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);

    }

    //METODO MOSTRAR PROFILE
    private void showEditProfileDialog() {
        /* Muestra las opciones contenidas
        1) Edit Profile Picture
        2) Edit Cover Photo
        3)Edit Name
        4) Edit Phone
         */
        //Opciones a mostrar
        String options [] = {"Editar foto de perfil","Editar portada","Editar nombre","Editar número de teléfono"};
        //Alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //Establece titulo
        builder.setTitle("Escoje una acción");
        //Establecer items al dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Manejador de los items clicks
                if (which == 0){
                //Si presiono Editar foto de perfil

                    pd.setMessage("Cargando foto de perfil...");
                    perfil_O_Portada = "imagen";
                    showImagePicDialog();
                }else if(which == 1){
                    //Si presiono Editar portada
                    pd.setMessage("Cargando foto de portada...");
                    perfil_O_Portada = "cover";
                    showImagePicDialog();
                }else if(which == 2){
                    //Si presiono Editar nombre
                    pd.setMessage("Actualizando nombre...");
                    //Llamando el metodo y pasando la clave "nombre" como parametro para actualizar en bd
                    showNamePhoneUpdateDialog("nombre");
                }else if(which == 3){
                    //Si presiono Editar telefono
                    pd.setMessage("Editando teléfono...");
                    showNamePhoneUpdateDialog("telefono");
                }
            }
        });
        //Crea y muestra el dialogo
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        /* El parametro "key" contendra el valor:
            "name" que es la clave en la db del usuario utilizada para actualizar el nombre de usuario
            "phone" ''                                                  el numero de telefono
         */
        //CUSTOM DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Actualizar " + key); //EX Actuzalizar jey o nombre
        //Set layout
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //ADD EDIT TEXT
        final EditText editText= new EditText(getActivity());
        editText.setHint("Ingresa tu nuevo " + key);// + /*+ key*/
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //ADD BUTTONS IN DIALOG ACTUALIZAR
        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //INput text from edittext
                final String value = editText.getText().toString().trim();
                //Validacion si el user ingreso algo
                if(!TextUtils.isEmpty(value)){
                    pd.show();

                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                // Actualizado, descarta progreso
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Actualizando...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //OBTEN ERROR, MUESTRALO Y DESCARTA
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    //---------------------------------------------------------------------------------------
                    //Si el usuario cambia su nombre, tambien cambiará en sus POSTS
                    //---------------------------------------------------------------------------------------
                    if(key.equals("nombre")){
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uNombre").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //Actualiza el nombre del usuario actual en los comentarios
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comentarios")){
                                        String child1 = ""+dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comentarios").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uNombre").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }else{
                    Toast.makeText(getActivity(), "Por favor ingrese "+ key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //ADD BUTTONS IN DIALOG CANCELAR
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            }
        });
        //CREATE AND SHOW DIALOG
        builder.create().show();
    }

    //METODO PARA MOSTRAR IMAGEN
    private void showImagePicDialog() {
    //show dialog contiene opciones de carga de camara  y galeria para subir la imagen
        //Opciones a mostrar
        String options [] = {"Cámara", "Galería"};
        //Alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //Establece titulo
        builder.setTitle("Escoja una imagen desde: ");
        //Establecer items al dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Manejador de los items clicks
                if (which == 0){
                    //Si la camara es escogida
                    if(!revisarPermisosCamara()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                }else if(which == 1){
                    //Si presiono Galeria
                    if(!revisarPermisosAlmacenamiento()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }
                }
            }
        });
        //Crea y muestra el dialogo
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /* Este metodo es llamado cuando el usuario presiona Permitir o Denegar desde el Permission dialog
            Aqui se va a manejar los casos de permisos
         */
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                // Escogiendo desde la camara, primero checamos si la camara y almacenamiento tiene permisos
                if(grantResults.length >0){
                    boolean cameraAceptada = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAceptado = grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if(cameraAceptada && storageAceptado){
                        //PERMISOS ACTIVADOS
                        pickFromCamera();
                    }else{
                        //PERMISOS DENEGADOS
                        Toast.makeText(getActivity(), "Necesitas otorgar permisos a la camara y al almacenamiento", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // Escogiendo desde galeria
                if (grantResults.length > 0) {
                    boolean writeStorageAcepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAcepted) {
                        //PERMISOS ACTIVADOS
                        pickFromGallery();
                    } else {
                        //PERMISOS DENEGADOS
                        Toast.makeText(getActivity(), "Necesitas otorgar permisos de acceso a galería", Toast.LENGTH_SHORT).show();
                    }

                }
            }break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // este metodo sera llamado despues de escojer una imagen desde camara o galeria
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK__GALLERY_CODE){
            //IMAGEN ES ESCOGIDA DESDE GALERIA, OBTEN URI DE IMAGEN
            image_uri = data.getData();
            cargarFotoPortada(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //IMAGEN ES ESCOGIDA DESDE CAMERA, OBTEN URI DE IMAGEN
                cargarFotoPortada(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void cargarFotoPortada(final Uri uri) {
        //Show progress
        pd.show();

        // En lugar de crear funciones separadas para foto y para portada vamos a trabajar el mismo
        // para ambas
        //RUTA Y NOMBRE DE LA IMAGEN A ALMACENAR EN FIREBASE
        //EX. Users_Profile_Cover_Imgs/Imagen_e17238.jpg
        //EX. Users_Profile_Cover_Imgs/Imagen_e1235235.jpg
        String filePathANDName= rutaAlmacenamiento+ "" + perfil_O_Portada+"_"+user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathANDName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Imagen es cargada, ahora obtenemos el URl y almacenamos en la db
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();

                        //Revisamos si la imagen esta cargada O no Y SI FUE RECIBIDA
                        if(uriTask.isSuccessful()){
                            //IMG CARGADA
                            //cargar en database
                            HashMap<String, Object> results = new HashMap<>();
                            /* El primer parametro es profileCoverOPhoto que tendra el valor de photo o cover
                            El segundo parametro contiene la URL DE LA IMAGEN almacenada en firebase bd
                             */
                            results.put(perfil_O_Portada, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL EN DATABASE DEL USUARIO SI ES AGREGADO EXITOSAMENTE
                                            //DESCARTA BARRA PROGRESO
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Imagen cargada...", Toast.LENGTH_SHORT);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Error agregando url en la BD
                                            //Descarta barra progreso
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error cargadando imagen...", Toast.LENGTH_SHORT);
                                        }
                                    });

                            //-------------------------------------------------------------------------------------------------------------
                            //Si el usuario cambia su nombre, tambien cambiará en sus POSTS
                            //---------------------------------------------------------------------------------------
                            if(perfil_O_Portada.equals("imagen")){
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //Actualiza la foto del usuario en los posts
                                //Actualiza el nombre del usuario actual en los comentarios
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            if(dataSnapshot.child(child).hasChild("Comentarios")){
                                                String child1 = ""+dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comentarios").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot ds: snapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            //-------------------------------------------------------------------------------------------------------------
                        }else{
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Un error inesperado ocurrió...", Toast.LENGTH_SHORT);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    // Existen errores, obten error y show, descarrta progreso de la barra
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromGallery() {
        //Intent de escoger desde galeria
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK__GALLERY_CODE);
    }

    private void pickFromCamera() {
        //Intent de escoger desde camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //Pon IMG URI
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void RevisarEstatusUsuario(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //VALIDACION
        if(user != null){
            //user is signed and stay here
            // Establece emaildel logueado
            // mProfileTv.setText(user.getEmail());
            uid = user.getUid();
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

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchveiw
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //LLAMADO CUANDO PRESIONA EL BOTON DE BUSQUEDA
                if(!TextUtils.isEmpty(query)){
                    //Busca
                    buscarMisPosts(query);
                }else{
                    cargarMisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //LLAMADO CUANDO SE PRESIONA CUALQUIER LETRA
                //LLAMADO CUANDO PRESIONA EL BOTON DE BUSQUEDA
                if(!TextUtils.isEmpty(newText)){
                    //Busca
                    buscarMisPosts(newText);
                }else{
                    cargarMisPosts();
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