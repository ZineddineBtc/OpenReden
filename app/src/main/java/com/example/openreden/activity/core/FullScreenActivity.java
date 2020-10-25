package com.example.openreden.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Objects;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView fullScreenIV;
    private ProgressBar progressBar;
    private FirebaseStorage storage;
    private String from, profileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        initializeInstances();
        findViewsByIds();
    }
    private void initializeInstances(){
        Objects.requireNonNull(getSupportActionBar()).hide();
        storage = FirebaseStorage.getInstance();
        from = getIntent().getStringExtra(StaticClass.FROM);
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        String photoSignature = getIntent().getStringExtra(StaticClass.PHOTO_SIGNATURE);
        getPhoto(profileID, photoSignature);
    }
    private void findViewsByIds(){
        fullScreenIV = findViewById(R.id.fullScreenIV);
        progressBar = findViewById(R.id.progressBar);
    }
    private void getPhoto(String profileID, String photoSignature){
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(profileID + photoSignature)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToPhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToPhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        fullScreenIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, fullScreenIV.getWidth(),
                fullScreenIV.getHeight(), false));
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        if(from.equals(StaticClass.PROFILE_FRAGMENT)){
            startActivity(new Intent(getApplicationContext(), CoreActivity.class)
            .putExtra(StaticClass.TO, StaticClass.PROFILE_FRAGMENT));
        }else{
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
            .putExtra(StaticClass.PROFILE_ID, profileID));
        }
    }
}
