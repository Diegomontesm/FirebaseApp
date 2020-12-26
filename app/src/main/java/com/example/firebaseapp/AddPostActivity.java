package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    //VISTAS
    EditText tituloEt, descripcionEt;
    ImageView imagenIv;
    Button publicarBTN;

    //INFO DEL USUARIO
    String nombre, email, uid, dp;

    //INFORMACIÓN DEL POST QUE SERÁ EDITADO
    String editarTitulo, editarDescripcion, editarImagen;

    //Imagen escogida estara en esta URI
    Uri image_rui = null;

    //ProgressDialog
    ProgressDialog pd;

    //Constantes de permisos de acceso a la camara y la galeria
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //Constantes de permisos de imagen
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //Permisos del array
    String[] PermisosDeCamara;
    String[] PermisosDeAlmacenamiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Nuevo estado");

        //ACTIVAR BOTON RETROCESO
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //INICIALIZAR PERMISOS DE CAMARA Y ALMACENAMIENTO ARRAY[]
        PermisosDeCamara = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermisosDeAlmacenamiento = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //INICIALIZAR
        firebaseAuth = FirebaseAuth.getInstance();
        RevisarEstatusUsuario();

        //INICIALIZAR VISTAS
        tituloEt = findViewById(R.id.pTitleEt);
        descripcionEt = findViewById(R.id.pDescriptionEt);
        imagenIv = findViewById(R.id.pImageIv);
        publicarBTN = findViewById(R.id.pUploadBTN);

        //OBTENER INFORMACION A TRAVES DEL INTENT DESDE EL ADAPTERPOST.java
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editarPostId");

        //Validamos si viene aqui para actualizar el post ejemplo. viene de AdapterPost
        if(isUpdateKey.equals("editarPost")){
            //Actualizar
            actionBar.setTitle("Actualizar Post");
            publicarBTN.setText("Actualizar");
            cargarInfoPost(editPostId);
        }else{
            //Agregar
            actionBar.setTitle("Agregar nuevo post");
            publicarBTN.setText("Cargar");
        }

        actionBar.setSubtitle(email);

        //INICIZLIZAR PD
        pd = new ProgressDialog(this);

        //Obten info del usuario actual para incluir en el post
        userDbRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    nombre = ""+ds.child("nombre").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("imagen").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //PUBLICAR BTN Click Listener
        publicarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtener informacion (Titulo, descrip) desde los EditText
                String titulo = tituloEt.getText().toString().trim();
                String descripcion = descripcionEt.getText().toString().trim();
                if(TextUtils.isEmpty(titulo)){
                    Toast.makeText(AddPostActivity.this, "Ingrese título...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(descripcion)){
                    Toast.makeText(AddPostActivity.this, "Ingrese descripcion", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editarPost")){
                    empiezaActualizar(titulo, descripcion, editPostId);
                }else{
                    actualizarData(titulo, descripcion);
                }
            }
        });

        //OBTENER IMAGEN DE LA CAMARA O DE LA GALERIA
        imagenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Muestra imagen pick dialog
                muestraImagenPickDialog();
            }
        });

    }

    private void empiezaActualizar(String titulo, String descripcion, String editPostId) {
        pd.setMessage("Actualizando post...");
        pd.show();

        if(!editarImagen.equals("SinImagen")){
            //Con imagen
            actualizarConImagen(titulo, descripcion, editPostId);
        }
        else if(imagenIv.getDrawable() != null){
            //Con imagen
            ActualizarConNuevaImagen(titulo, descripcion, editPostId);
        }
        else{
            //Sin imagen, se mantiene sin imagen en imageview
            actualizarSinImagen(titulo, descripcion, editPostId);
        }
    }

    private void actualizarSinImagen(String titulo,String descripcion,String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //pon la info del post
        hashMap.put("uid", uid);
        hashMap.put("uNombre", nombre);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitulo", titulo);
        hashMap.put("pDescripcion", descripcion);
        hashMap.put("pImagen", "SinImagen");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Actualizando...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ActualizarConNuevaImagen(final String titulo, final String descripcion, final String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String archivoRutaYNombre = "Posts/" + "post_" + timeStamp;

        //OBTEN LA IMAGEN DEL IMAGEVIEW
        Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //Comprime la imagen
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(archivoRutaYNombre);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //IMAGEN CARGADA OBTEN SU URL
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if(uriTask.isSuccessful()){
                    //URL es recibida, cargalo a la DB
                    HashMap<String, Object> hashMap = new HashMap<>();
                    //pon la info del post
                    hashMap.put("uid", uid);
                    hashMap.put("uNombre", nombre);
                    hashMap.put("uEmail", email);
                    hashMap.put("uDp", dp);
                    hashMap.put("pTitulo", titulo);
                    hashMap.put("pDescripcion", descripcion);
                    hashMap.put("pImagen", downloadUri);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(editPostId).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Actualizando...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarConImagen(final String titulo, final String descripcion, final String editPostId) {
        //Post tiene imagen, borrar imagen previa primero
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editarImagen);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Imagen borrada, cargar nueva imagen
                        //post-imagen nombre, post-id, publicarhora
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String archivoRutaYNombre = "Posts/" + "post_" + timeStamp;

                        //OBTEN LA IMAGEN DEL IMAGEVIEW
                        Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //Comprime la imagen
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(archivoRutaYNombre);
                        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //IMAGEN CARGADA OBTEN SU URL
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();
                                if(uriTask.isSuccessful()){
                                    //URL es recibida, cargalo a la DB
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    //pon la info del post
                                    hashMap.put("uid", uid);
                                    hashMap.put("uNombre", nombre);
                                    hashMap.put("uEmail", email);
                                    hashMap.put("uDp", dp);
                                    hashMap.put("pTitulo", titulo);
                                    hashMap.put("pDescripcion", descripcion);
                                    hashMap.put("pImagen", downloadUri);

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                    ref.child(editPostId).updateChildren(hashMap)
                                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                       public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Actualizando...", Toast.LENGTH_SHORT).show();
                                       }
                                       })
                                       .addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                       }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //IMAGEN NO CARGADA,
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
            .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Imagen no fue actualizada
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarInfoPost(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //Obtener detalles del post usando el id del post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //OBTEN LA INFORMACION
                    editarTitulo = ""+ds.child("pTitulo").getValue();
                    editarDescripcion = ""+ds.child("pDescripcion").getValue();
                    editarImagen = ""+ds.child("pImagen").getValue();
                    //ESTABLECE LA INFORMACION A LAS VISTAS
                    tituloEt.setText(editarTitulo);
                    descripcionEt.setText(editarDescripcion);
                    //ESTABLECER IMAGEN
                    if(!editarImagen.equals("SinImagen")){
                        try{
                            Picasso.get().load(editarImagen).into(imagenIv);
                        }catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actualizarData(final String titulo, final String descripcion) {
    pd.setMessage("Publicando post...");
    pd.show();

    //PARA POSTIMAGE NOMBRE, POST-ID, POST-PUBLISHDATE
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String archivoRutaYNombre = "Posts/" + "post_" + timestamp;

        if(imagenIv.getDrawable() != null){
            //OBTEN LA IMAGEN DEL IMAGEVIEW
            Bitmap bitmap = ((BitmapDrawable)imagenIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Comprime la imagen
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
        //POST CON IMAGEN
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(archivoRutaYNombre);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //imagen es cargada a firebase storage, obten su uri
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                                String descargarUri = uriTask.getResult().toString();

                                if(uriTask.isSuccessful()){
                                //URI ES ES RECIBIDO Y CARGADO A POST DE FIREBADSE DB
                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    //PUT POST INFO
                                    hashMap.put("uid", uid);
                                    hashMap.put("uNombre", nombre);
                                    hashMap.put("uEmail", email);
                                    hashMap.put("uDp", dp);
                                    hashMap.put("pId", timestamp);
                                    hashMap.put("pTitulo", titulo);
                                    hashMap.put("pDescripcion", descripcion);
                                    hashMap.put("pImagen", descargarUri);
                                    hashMap.put("pHora", timestamp);

                                    //Ruta para almacenar la info del POST
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                    //Pon info y esta referencia
                                    ref.child(timestamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                //AGREGANDO A LA DB
                                                    pd.dismiss();
                                                    Toast.makeText(AddPostActivity.this, "Post publicado", Toast.LENGTH_SHORT).show();
                                                    //reset
                                                    tituloEt.setText("");
                                                    descripcionEt.setText("");
                                                    imagenIv.setImageURI(null);
                                                    image_rui =null;
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //FALLO AGREGANDO INFO EN POST
                                            pd.dismiss();
                                            //muestra error
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Fallo en cargar imagen
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
        //POST SIN IMAGEN
            HashMap<Object, String> hashMap = new HashMap<>();
            //PUT POST INFO
            hashMap.put("uid", uid);
            hashMap.put("uNombre", nombre);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timestamp);
            hashMap.put("pTitulo", titulo);
            hashMap.put("pDescripcion", descripcion);
            hashMap.put("pImagen", "SinImagen");
            hashMap.put("pHora", timestamp);

            //Ruta para almacenar la info del POST
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //Pon info y esta referencia
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //AGREGANDO A LA DB
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post publicado", Toast.LENGTH_SHORT).show();
                            //reset
                            tituloEt.setText("");
                            descripcionEt.setText("");
                            imagenIv.setImageURI(null);
                            image_rui =null;

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //FALLO AGREGANDO INFO EN POST
                    pd.dismiss();
                    tituloEt.setText("");
                    descripcionEt.setText("");
                    imagenIv.setImageURI(null);
                    image_rui =null;
                    //muestra error
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void muestraImagenPickDialog() {
        //Opciones(Camera, galería) para mostrar en el PickDialog
        String[] opciones = {"Cámara","Galería"};

        //Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escoje una imagen desde:");
        //Establece las opciones del dialog
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Manejador de clicks
                if(which == 0){
                    //La camara ha sido escogida
                    if(!RevisarPermisosDeCamara()){
                        SolicitarPermisosDeCamara();
                    }else{
                        escojeDesdeCamara();
                    }
                }
                if(which == 1){
                    //La galeria ha sido escogida
                    if(!RevisarPermisosDeAlmacenamiento()){
                        SolicitarPermisosDeAlmacenamiento();
                    }else{
                        escojerDesdeGaleria();
                    }
                }
            }
        });
        //Crea y muestra el dialog
        builder.create().show();
    }

    private void escojerDesdeGaleria() {
        //Intent de escoger imagen desde la galeria
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void escojeDesdeCamara() {
    //Intent para escojer imagen desde la camara
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Imagen temporal");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion temporal");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean RevisarPermisosDeAlmacenamiento(){
        // Revisa si los permisos de almacenamiento están activados o no
        // Regresa TRUE si si lo están, FALSE si NO lo están
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void SolicitarPermisosDeAlmacenamiento(){
        //Solicitar tiempo de ejecucion de los permisos de almacenamiento
        ActivityCompat.requestPermissions(this, PermisosDeAlmacenamiento, STORAGE_REQUEST_CODE);
    }

    private boolean RevisarPermisosDeCamara(){
        // Revisa si los permisos de la camara están activados o no
        // Regresa TRUE si si lo están, FALSE si NO lo están
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void SolicitarPermisosDeCamara(){
        //Solicitar tiempo de ejecucion de los permisos de la camara
        ActivityCompat.requestPermissions(this, PermisosDeCamara, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        RevisarEstatusUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RevisarEstatusUsuario();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //IR AL ACTIVITY PREVIO
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void RevisarEstatusUsuario(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //VALIDACION
        if(user != null){
            //user is signed and stay here
            // Establece emaildel logueado
            email = user.getEmail();
            uid = user.getUid();
            // mProfileTv.setText(user.getEmail());
        }else{
            //user isnt signed fo to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
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

    //Manejador de los resultados de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Este metodo es llamado cuando un usuario presiona "Permitir" o "Denegar" desde el Permision Request Dialog
        //Aqui manejamos los casos (PERMITIDO O DENEGADO)
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean camaraAceptada = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean almacenamientoAceptado = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(camaraAceptada && almacenamientoAceptado){
                        //Ambos permisos son aceptados
                        escojeDesdeCamara();
                    }else{
                        //Camara O Galeria O ambos son denegados
                        Toast.makeText(this, "Cámara y Galería ambos servicios son necesarios...", Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean almacenamientoAceptado = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(almacenamientoAceptado){
                        //Los permisos de almacenamiento son aceptados
                        escojerDesdeGaleria();
                    }else{
                        //Camara O Galeria O ambos son denegados
                        Toast.makeText(this, "Permisos de la Galería son necesarios...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Este metodo sera llamado despues de escojer una imag de galeria o camara
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //Imagen es escogida desde la galeria , obten su uri
                image_rui = data.getData();

                //Establece en iamgeView
                imagenIv.setImageURI(image_rui);
            }else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //Imagen es escogida desde la camara, obten su uri
                imagenIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}