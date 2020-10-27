package com.example.openreden.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.GalleryAdapter;
import com.example.openreden.model.Photo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

public class ProfileActivity extends AppCompatActivity {

    private ImageView photoIV;
    private TextView usernameTV, nameTV, bioTV, emptyGalleryTV;
    private ViewPager galleryVP;
    private GalleryAdapter galleryAdapter;
    private ArrayList<Photo> galleryPhotos = new ArrayList<>();
    private LinearLayout galleryDotsLL;
    private ImageView[] dots;
    private int dotsCount;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private String profileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getInstances();
        findViewsByIds();
        getProfilePhoto();
        setProfileData();
    }
    private void getInstances(){
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        nameTV = findViewById(R.id.nameTV);
        bioTV = findViewById(R.id.bioTV);
        emptyGalleryTV = findViewById(R.id.emptyGalleryTV);
        galleryVP = findViewById(R.id.galleryVP);
        galleryDotsLL = findViewById(R.id.galleryDotsLL);
    }
    private void getProfilePhoto(){
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(profileID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToProfilePhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToProfilePhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void setProfileData(){
        database.collection("users")
                .document(profileID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            usernameTV.setText(String.valueOf(document.get("username")));
                            nameTV.setText(String.valueOf(document.get("name")));
                            bioTV.setText(String.valueOf(document.get("bio")));
                            getGalleryPhotos((ArrayList<String>)document.get("gallery"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at setting profile date", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void getGalleryPhotos(ArrayList<String> photosReferences){
        if(!photosReferences.isEmpty()){
            final long ONE_MEGABYTE = 1024 * 1024 * 2;
            for(final String reference: photosReferences){
                storage.getReference(reference)
                        .getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                galleryPhotos.add(new Photo(
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length),
                                        reference));
                                setGallery();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Failed at getting gallery", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }else{
            galleryVP.setVisibility(View.GONE);
            emptyGalleryTV.setVisibility(View.VISIBLE);
        }
    }
    private void setGallery(){
        galleryVP.setVisibility(galleryPhotos.isEmpty() ? View.GONE : View.VISIBLE);
        emptyGalleryTV.setVisibility(!galleryPhotos.isEmpty() ? View.GONE : View.VISIBLE);
        if(galleryPhotos.isEmpty()) {
            return;
        }
        galleryAdapter = new GalleryAdapter(getApplicationContext(), galleryPhotos);
        galleryVP.setAdapter(galleryAdapter);
        setGalleryDotsLL();
    }
    private void setGalleryDotsLL(){
        dotsCount = galleryAdapter.getCount();
        galleryDotsLL.removeAllViews();
        dots = new ImageView[dotsCount];
        for(int i = 0; i < dotsCount; i++){
            dots[i] = new ImageView(getApplicationContext());
            dots[i].setImageDrawable(getDrawable(R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            galleryDotsLL.addView(dots[i], params);
        }
        dots[0].setImageDrawable(getDrawable(R.drawable.active_dot));
        galleryVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< dotsCount; i++){
                    dots[i].setImageDrawable(getDrawable(R.drawable.nonactive_dot));
                }
                dots[position].setImageDrawable(getDrawable(R.drawable.active_dot));
            }
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}@Override public void onPageScrollStateChanged(int state) {}
        });
    }
    public void message(View view){
        //open discussion activity
    }
}
