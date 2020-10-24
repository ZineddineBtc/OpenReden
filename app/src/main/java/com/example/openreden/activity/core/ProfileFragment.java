package com.example.openreden.activity.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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
import androidx.fragment.app.Fragment;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private ImageView photoIV, editUsernameIV, editNameIV, editBioIV;
    private TextView usernameTV, nameTV, bioTV, emailTV, signOutTV, errorTV;
    private EditText usernameET, nameET, bioET;
    private LinearLayout galleryLL;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String username, name, bio, email;
    private boolean usernameETShown, nameETShown, bioETShown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        context = fragmentView.getContext();
        getInstances();
        findViewsByIds();
        setUserData();
        getPhoto();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        editor = sharedPreferences.edit();
    }
    private void findViewsByIds(){
        photoIV = fragmentView.findViewById(R.id.photoIV);
        usernameTV = fragmentView.findViewById(R.id.usernameTV);
        usernameET = fragmentView.findViewById(R.id.usernameET);
        nameTV = fragmentView.findViewById(R.id.nameTV);
        nameET = fragmentView.findViewById(R.id.nameET);
        bioTV = fragmentView.findViewById(R.id.bioTV);
        bioET = fragmentView.findViewById(R.id.bioET);
        emailTV = fragmentView.findViewById(R.id.emailTV);
        signOutTV = fragmentView.findViewById(R.id.signOutTV);
        errorTV = fragmentView.findViewById(R.id.errorTV);
        galleryLL = fragmentView.findViewById(R.id.galleryLL);
        setGalleryHeight();
        editUsernameIV = fragmentView.findViewById(R.id.editUsernameIV);
        editNameIV = fragmentView.findViewById(R.id.editNameIV);
        editBioIV = fragmentView.findViewById(R.id.editBioIV);
    }
    private void setGalleryHeight(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        galleryLL.setMinimumHeight(width/3);
    }
    private void getPhoto(){
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(email + StaticClass.profilePhoto)
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
    private void setUserData(){
        photoIV.setDrawingCacheEnabled(true);
        photoIV.buildDrawingCache();
        username = sharedPreferences.getString(StaticClass.USERNAME, "no username");
        usernameTV.setText(username);
        usernameET.setText(username);
        name = sharedPreferences.getString(StaticClass.NAME, "no name");
        nameTV.setText(name);
        nameET.setText(name);
        bio = sharedPreferences.getString(StaticClass.BIO, "no bio");
        bioTV.setText(bio);
        bioET.setText(bio);
        emailTV.setText(email);
        setListeners();
    }
    private void setListeners(){
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
    }
    private void editUsername(){
        if(nameETShown || bioETShown){
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
        database.collection("app-data")
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
        database.collection("app-data")
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
        if(usernameETShown || bioETShown){
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
        if(usernameETShown || nameETShown){
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
