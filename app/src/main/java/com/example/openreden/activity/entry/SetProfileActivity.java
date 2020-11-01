package com.example.openreden.activity.entry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.activity.TermsActivity;
import com.example.openreden.activity.core.CoreActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class SetProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView errorTV;
    private EditText usernameET, nameET, bioET, cityET;
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> usernameList = new ArrayList<>();
    private String username, name, bio, email, city;
    private boolean imagePicked, usernameAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        Objects.requireNonNull(getSupportActionBar()).hide();
        initializeInstances();
        findViewsByIds();
        checkBuildVersion();
        getUsernameList();
    }
    private void initializeInstances(){
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
    }
    public void checkBuildVersion(){
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyHavePermission()) {
                requestForSpecificPermission();
            }
        }
    }
    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET},
                101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // not granted
                moveTaskToBack(true);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void findViewsByIds(){
        errorTV = findViewById(R.id.errorTV);
        photoIV = findViewById(R.id.photoIV);
        usernameET = findViewById(R.id.usernameET);
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(usernameList.contains(String.valueOf(s)) || count<4){
                    usernameET.setTextColor(getColor(R.color.dark_red));
                    usernameAvailable = false;
                }else{
                    usernameET.setTextColor(getColor(R.color.green));
                    usernameAvailable = true;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
        nameET = findViewById(R.id.nameET);
        bioET = findViewById(R.id.bioET);
        cityET = findViewById(R.id.cityET);
        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishRegister();
            }
        });
    }
    public void importImage(View view){
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Images"),
                StaticClass.PICK_SINGLE_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticClass.PICK_SINGLE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if(uri != null){
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = getApplicationContext().getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                            getApplicationContext().getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "IO Exception when selecting a profile image",
                            Toast.LENGTH_LONG).show();
                }
                photoIV.setImageBitmap(imageBitmap);
                imagePicked = true;
            }
        }
    }
    private void getUsernameList(){
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        database.collection("app-data")
                .document("usernames")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            usernameList = (ArrayList<String>) document.get("usernames");
                            progressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at downloading usernameList", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
    }
    private void finishRegister(){
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(!imagePicked){
            displayErrorTV(R.string.no_photo_selected);
            return;
        }
        username = usernameET.getText().toString().trim();
        if(username.length()<4){
            displayErrorTV(R.string.insufficient_username);
            return;
        }
        if(!usernameAvailable){
            displayErrorTV(R.string.username_taken);
            return;
        }
        name = nameET.getText().toString().trim();
        if(StaticClass.containsDigit(name) || name.length()<=1){
            displayErrorTV(R.string.invalid_name);
            return;
        }
        bio = bioET.getText().toString().trim();
        if(bio.isEmpty()){
            displayErrorTV(R.string.empty_bio);
            return;
        }
        city = cityET.getText().toString().trim();
        if(city.length()<2){
            displayErrorTV(R.string.invalid_city);
            return;
        }
        progressDialog.show();
        progressDialog.setMessage("Setting up profile...");
        uploadPhoto();
    }
    private byte[] getPhotoData(){
        Bitmap bitmap = ((BitmapDrawable) photoIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void uploadPhoto(){
        byte[] data = getPhotoData();
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .putBytes(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Uploading photo failed", Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        writeSharedPreferences();
            }
        });
    }
    private void writeSharedPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(StaticClass.EMAIL, email);
        editor.putString(StaticClass.USERNAME, username);
        editor.putString(StaticClass.NAME, name);
        editor.putString(StaticClass.BIO, bio);
        editor.putString(StaticClass.CITY, city);
        editor.putStringSet(StaticClass.GALLERY, new HashSet<>(new ArrayList<String>()));
        editor.apply();
        appendUsernameToDB();
    }
    private void appendUsernameToDB(){
        database.collection("app-data")
                .document("usernames")
                .update("usernames", FieldValue.arrayUnion(username))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        writeOnlineDatabase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at appending username to database", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void writeOnlineDatabase(){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("username", username);
        userReference.put("name", name);
        userReference.put("bio", bio);
        userReference.put("city", city);
        userReference.put("gallery", new ArrayList<String>());
        userReference.put("chats", new ArrayList<String>());
        database.collection("users")
                .document(email)
                .set(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(getApplicationContext(), CoreActivity.class));
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }
    public void toTermsAndConditions(View view) {
        startActivity(new Intent(getApplicationContext(), TermsActivity.class));
    }
    public void displayErrorTV(int resourceID) {
        errorTV.setText(resourceID);
        errorTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTV.setVisibility(View.GONE);
            }
        }, 1500);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
