package com.example.openreden.activity.core.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.activity.core.FullScreenActivity;
import com.example.openreden.activity.entry.LoginActivity;
import com.example.openreden.adapter.GalleryAdapter;
import com.example.openreden.model.Photo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

public class ProfileFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private ImageView photoIV, editUsernameIV, editNameIV, editBioIV, editCityIV,
            addGalleryPhotoIV, deleteGalleryPhotoIV;
    private TextView usernameTV, nameTV, bioTV, cityTV, emailTV, galleryTV, emptyGalleryTV,
            signOutTV, errorTV;
    private EditText usernameET, nameET, bioET, cityET;
    private ViewPager galleryVP;
    private GalleryAdapter galleryAdapter;
    private LinearLayout galleryDotsLL;
    private ImageView[] dots;
    private int dotsCount;
    private LinearLayout viewPhotoLL, uploadPhotoLL;
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<String> galleryReferences;
    private ArrayList<Photo> photos = new ArrayList<>();
    private String username, name, city, email, galleryReference;
    private boolean usernameETShown, nameETShown, bioETShown, cityETShown;
    private byte[] profilePhotoData;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout shadeLL, photoOptionsLL;
    public static boolean photoOptionsShown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        getInstances();
        findViewsByIds();
        setUserData();
        getPhoto();
        getGallery();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        editor = sharedPreferences.edit();
        progressDialog = new ProgressDialog(context);
    }
    private void findViewsByIds(){
        photoIV = fragmentView.findViewById(R.id.photoIV);
        usernameTV = fragmentView.findViewById(R.id.usernameTV);
        usernameET = fragmentView.findViewById(R.id.usernameET);
        editUsernameIV = fragmentView.findViewById(R.id.editUsernameIV);
        nameTV = fragmentView.findViewById(R.id.nameTV);
        nameET = fragmentView.findViewById(R.id.nameET);
        editNameIV = fragmentView.findViewById(R.id.editNameIV);
        bioTV = fragmentView.findViewById(R.id.bioTV);
        bioET = fragmentView.findViewById(R.id.bioET);
        editBioIV = fragmentView.findViewById(R.id.editBioIV);
        cityTV = fragmentView.findViewById(R.id.cityTV);
        cityET = fragmentView.findViewById(R.id.cityET);
        editCityIV = fragmentView.findViewById(R.id.editCityIV);
        emailTV = fragmentView.findViewById(R.id.emailTV);
        galleryTV = fragmentView.findViewById(R.id.galleryTV);
        addGalleryPhotoIV = fragmentView.findViewById(R.id.addGalleryPhotoIV);
        deleteGalleryPhotoIV = fragmentView.findViewById(R.id.deleteGalleryPhotoIV);
        emptyGalleryTV = fragmentView.findViewById(R.id.emptyGalleryTV);
        galleryVP = fragmentView.findViewById(R.id.galleryVP);
        galleryDotsLL = fragmentView.findViewById(R.id.galleryDotsLL);
        signOutTV = fragmentView.findViewById(R.id.signOutTV);
        errorTV = fragmentView.findViewById(R.id.errorTV);
        shadeLL = fragmentView.findViewById(R.id.shadeLL);
        photoOptionsLL = fragmentView.findViewById(R.id.photoOptionsLL);
        viewPhotoLL = fragmentView.findViewById(R.id.viewPhotoLL);
        uploadPhotoLL = fragmentView.findViewById(R.id.uploadPhotoLL);
    }
    private void setUserData(){
        photoIV.setDrawingCacheEnabled(true);
        photoIV.buildDrawingCache();
        username = sharedPreferences.getString(StaticClass.USERNAME, "no username");
        usernameTV.setText(username);
        usernameET.setText(username);
        name = sharedPreferences.getString(StaticClass.NAME, "no name");
        nameTV.setText(name);
        nameET.setText(name);
        String bio = sharedPreferences.getString(StaticClass.BIO, "no bio");
        bioTV.setText(bio);
        bioET.setText(bio);
        city = sharedPreferences.getString(StaticClass.CITY, "no city");
        cityTV.setText(city);
        cityET.setText(city);
        emailTV.setText(email);
        setGallery();
        setListeners();
    }
    @SuppressLint("SetTextI18n")
    private void setGallery(){
        galleryReferences = new ArrayList<>(sharedPreferences.getStringSet(StaticClass.GALLERY, new HashSet<String>()));
        galleryTV.setText(getString(R.string.pictures)+"  ("+photos.size()+"/3)");
        galleryVP.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        emptyGalleryTV.setVisibility(!photos.isEmpty() ? View.GONE : View.VISIBLE);
        deleteGalleryPhotoIV.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        if(photos.isEmpty()) {
            return;
        }
        addGalleryPhotoIV.setVisibility(photos.size() == StaticClass.MAX_GALLERY_COUNT ?
                                        View.INVISIBLE : View.VISIBLE);
        galleryAdapter = new GalleryAdapter(context, photos);
        galleryVP.setAdapter(galleryAdapter);
        setGalleryDotsLL();
    }
    private void setGalleryDotsLL(){
        dotsCount = galleryAdapter.getCount();
        galleryDotsLL.removeAllViews();
        dots = new ImageView[dotsCount];
        for(int i = 0; i < dotsCount; i++){
            dots[i] = new ImageView(context);
            dots[i].setImageDrawable(context.getDrawable(R.drawable.nonactive_dot));
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            galleryDotsLL.addView(dots[i], params);
        }
        dots[0].setImageDrawable(context.getDrawable(R.drawable.active_dot));
        galleryVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< dotsCount; i++){
                    dots[i].setImageDrawable(context.getDrawable(R.drawable.nonactive_dot));
                }
                dots[position].setImageDrawable(context.getDrawable(R.drawable.active_dot));
            }
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}@Override public void onPageScrollStateChanged(int state) {}
        });
    }
    private void setListeners(){
        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoOptions();
            }
        });
        editUsernameIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUsername();
            }
        });
        editNameIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editName();
            }
        });
        editBioIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBio();
            }
        });
        editCityIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCity();
            }
        });
        addGalleryPhotoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importImage();
            }
        });
        signOutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySignOutDialog();
            }
        });
        viewPhotoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPhoto();
            }
        });
        uploadPhotoLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { if(photos.size()<StaticClass.MAX_GALLERY_COUNT) importImage(); }
        });
        deleteGalleryPhotoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhotoGallery();
            }
        });
    }
    private void getPhoto(){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        storage.getReference(email + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToPhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToPhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void getGallery(){
        photos.clear();
        if(galleryAdapter!=null) galleryAdapter.notifyDataSetChanged();
        if(!galleryReferences.isEmpty()){
            final long ONE_MEGABYTE = 1024 * 1024 * 2;
            for(final String reference: galleryReferences){
                storage.getReference(reference)
                        .getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                photos.add(new Photo(
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length),
                                        reference));
                                setGallery();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(context, "Failed at getting gallery", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }else{
            galleryVP.setVisibility(View.GONE);
            emptyGalleryTV.setVisibility(View.VISIBLE);
        }
    }
    private void importImage(){
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
                Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if(uri != null){
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = context.getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(context, "IO Exception when selecting a profile image",
                            Toast.LENGTH_LONG).show();
                }
                if(photoOptionsShown) {
                    photoIV.setImageBitmap(imageBitmap);
                    changePhoto();
                }else{
                    uploadGalleryPhoto(imageBitmap);
                }
            }
        }
    }
    private byte[] getProfilePhotoData(){
        Bitmap bitmap = ((BitmapDrawable) photoIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void showPhotoOptions(){
        shadeLL.setVisibility(View.VISIBLE);
        photoOptionsLL.setVisibility(View.VISIBLE);
        photoOptionsShown = true;
    }
    private void viewPhoto(){
        startActivity(new Intent(context, FullScreenActivity.class)
        .putExtra(StaticClass.FROM, StaticClass.PROFILE_FRAGMENT)
        .putExtra(StaticClass.PROFILE_ID, email)
        .putExtra(StaticClass.PHOTO_SIGNATURE, StaticClass.PROFILE_PHOTO));
    }
    private void changePhoto(){
        profilePhotoData = getProfilePhotoData();
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        deletePhoto();
    }
    private void deletePhoto(){
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uploadPhoto();
            }
        });
    }
    private void uploadPhoto(){
        storage.getReference().child(email+StaticClass.PROFILE_PHOTO)
                .putBytes(profilePhotoData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Uploaded!", Toast.LENGTH_LONG).show();
                        shadeLL.setVisibility(View.GONE);
                        photoOptionsLL.setVisibility(View.GONE);
                        photoOptionsShown = false;
                    }
        });
    }
    private void editUsername(){
        if(nameETShown || bioETShown || cityETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(usernameETShown){
            String newUsername = usernameET.getText().toString().trim();
            if(newUsername.length()>4) {
                if(!username.equals(newUsername)) {
                    checkIfUsernameTaken(newUsername);
                }
            }else{
                displayErrorTV(R.string.insufficient_username);
            }
        }else{
            toggleUsername();
        }

    }
    private void toggleUsername(){
        usernameET.setVisibility(!usernameETShown ? View.VISIBLE : View.GONE);
        usernameTV.setVisibility(usernameETShown ? View.VISIBLE : View.GONE);
        editUsernameIV.setImageDrawable(usernameETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!usernameETShown){
            usernameET.requestFocus();
        }
        usernameETShown = !usernameETShown;
    }
    private void checkIfUsernameTaken(final String newUsername){
        database.collection("app-profilePhotoData")
                .document("usernames").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> usernames = (ArrayList<String>) document.get("usernames");
                        if(usernames.contains(newUsername)){
                            displayErrorTV(R.string.username_taken);
                        }else{
                            adjustUsernameList(username, newUsername);
                            writeUsername(newUsername);
                        }
                    }
                } else {
                    Toast.makeText(fragmentView.getContext(),
                            "Failed at checking the availability of the username",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void adjustUsernameList(String oldUsername, String newUsername){
        database.collection("app-profilePhotoData")
                .document("usernames")
                .update("usernames", FieldValue.arrayRemove(oldUsername),
                        "usernames", FieldValue.arrayUnion(newUsername));
    }
    private void writeUsername(final String username){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("username", username);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.USERNAME, username);
                        editor.apply();
                        toggleUsername();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Username updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editName(){
        if(usernameETShown || bioETShown || cityETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(nameETShown){
            String newName = nameET.getText().toString();
            if(!StaticClass.containsDigit(newName) && newName.length()>2){
                if(!name.equals(newName)){
                    writeName(newName);
                }
            }else{
                displayErrorTV(R.string.invalid_name);
            }
        }else{
            toggleName();
        }
    }
    private void toggleName(){
        nameET.setVisibility(!nameETShown ? View.VISIBLE : View.GONE);
        nameTV.setVisibility(nameETShown ? View.VISIBLE : View.GONE);
        editNameIV.setImageDrawable(nameETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!nameETShown){
            nameET.requestFocus();
        }
        nameETShown = !nameETShown;
    }
    private void writeName(final String name){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("name", name);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.NAME, name);
                        editor.apply();
                        toggleName();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Name updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing name",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editBio(){
        if(usernameETShown || nameETShown || cityETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(bioETShown){
            String newBio = bioET.getText().toString();
            writeBio(newBio);
        }else{
            toggleBio();
        }
    }
    private void toggleBio(){
        bioET.setVisibility(!bioETShown ? View.VISIBLE : View.GONE);
        bioTV.setVisibility(bioETShown ? View.VISIBLE : View.GONE);
        editBioIV.setImageDrawable(bioETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!bioETShown){
            bioET.requestFocus();
        }
        bioETShown = !bioETShown;
    }
    private void writeBio(final String bio){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("bio", bio);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.BIO, bio);
                        editor.apply();
                        toggleBio();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Bio updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing name",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editCity(){
        if(usernameETShown || nameETShown || bioETShown){
            Toast.makeText(context, "Pending edit", Toast.LENGTH_LONG).show();
            return;
        }
        if(cityETShown){
            String newCity = cityET.getText().toString();
            if(newCity.length()>2){
                if(!city.equals(newCity)){
                    writeCity(newCity);
                }
            }else{
                displayErrorTV(R.string.invalid_city);
            }
        }else{
            toggleCity();
        }
    }
    private void toggleCity(){
        cityET.setVisibility(!cityETShown ? View.VISIBLE : View.GONE);
        cityTV.setVisibility(cityETShown ? View.VISIBLE : View.GONE);
        editCityIV.setImageDrawable(cityETShown ?
                context.getDrawable(R.drawable.ic_edit) :
                context.getDrawable(R.drawable.ic_check));
        if(!cityETShown){
            cityET.requestFocus();
        }
        cityETShown = !cityETShown;
    }
    private void writeCity(final String city){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("city", city);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        editor.putString(StaticClass.CITY, city);
                        editor.apply();
                        toggleCity();
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "City updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing name",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private byte[] getGalleryPhotoData(Bitmap imageBitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void uploadGalleryPhoto(Bitmap imageBitmap){
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        byte[] galleryPhotoData = getGalleryPhotoData(imageBitmap);
        final String storageReference = email+"-"+StaticClass.getCurrentTime();
        photos.add(new Photo(imageBitmap, storageReference));
        storage.getReference().child(storageReference)
                .putBytes(galleryPhotoData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, "Failure at uploading gallery photo", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(context, "Uploaded!", Toast.LENGTH_LONG).show();
                        addGalleryPhotoReference(storageReference);
                        setGallery();
                        progressDialog.dismiss();
                    }
                });
    }
    private void addGalleryPhotoReference(String storageReference){
        database.collection("users")
                .document(email)
                .update("gallery", FieldValue.arrayUnion(storageReference));
        galleryReferences.add(storageReference);
        editor.putStringSet(StaticClass.GALLERY, new HashSet<>(galleryReferences));
        editor.apply();
    }
    private void deletePhotoGallery(){
        progressDialog.setMessage("Deleting...");
        progressDialog.show();
        int galleryReferenceIndex = galleryVP.getCurrentItem();
        galleryReference = photos.get(galleryReferenceIndex).getStorageReference();
        storage.getReference()
                .child(galleryReference)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteOnlineGalleryReference(galleryReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed at deleting picture", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void deleteOnlineGalleryReference(final String galleryReference){
        database.collection("users")
                .document(email)
                .update("gallery", FieldValue.arrayRemove(galleryReference))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteLocalGalleryReference();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed at deleting picture", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void deleteLocalGalleryReference(){
        galleryReferences.remove(galleryReference);
        int removeIndex = -1;
        for(int i=0; i<photos.size(); i++) {
            if (photos.get(i).getStorageReference().equals(galleryReference)) {
                removeIndex = i;
                break;
            }
        }
        if(removeIndex!=-1) photos.remove(removeIndex);
        editor.putStringSet(StaticClass.GALLERY, new HashSet<>(galleryReferences));
        editor.apply();
        setGallery();
        progressDialog.dismiss();
    }
    private void displaySignOutDialog(){
        try {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Sign out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton(
                            Html.fromHtml("<font color=\"#FF0000\"> Sign out </font>")
                            , new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    signOut();
                                }
                            })
                    .setNegativeButton(
                            Html.fromHtml("<font color=\"#1976D2\"> Cancel </font>"),
                            null)
                    .show();
        }catch (NullPointerException e){
            Toast.makeText(context, "Failed at showing AlertDialog", Toast.LENGTH_SHORT).show();
            signOut();
        }
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(fragmentView.getContext(), LoginActivity.class));
    }
    private void displayErrorTV(int resourceID) {
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
    private void hideKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager)
                fragmentView.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}
