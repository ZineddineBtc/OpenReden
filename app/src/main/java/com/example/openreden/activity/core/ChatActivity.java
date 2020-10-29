package com.example.openreden.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.MessageAdapter;
import com.example.openreden.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView interlocutorPhotoIV, backIV;
    private TextView interlocutorUsernameTV, interlocutorNameTV;
    private EditText textET;
    private RecyclerView messagesRV;
    private MessageAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String email, profileID, from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getInstances();
        findViewsByIds();
        setInterlocutorPhoto();
        setMessagesRV();
        getChatReference();
    }
    private void getInstances(){
        Objects.requireNonNull(getSupportActionBar()).hide();
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        from = getIntent().getStringExtra(StaticClass.FROM);
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
    }
    private void findViewsByIds(){
        toolbar = findViewById(R.id.toolbar);
        backIV = toolbar.findViewById(R.id.backIV);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        interlocutorPhotoIV = toolbar.findViewById(R.id.interlocutorPhotoIV);
        interlocutorUsernameTV = toolbar.findViewById(R.id.interlocutorUsernameTV);
        interlocutorNameTV = toolbar.findViewById(R.id.interlocutorNameTV);
        textET = findViewById(R.id.textET);
        messagesRV = findViewById(R.id.messagesRV);
    }
    private void setInterlocutorPhoto(){
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(profileID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setInterlocutorName();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                interlocutorPhotoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, interlocutorPhotoIV.getWidth(),
                        interlocutorPhotoIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed at getting interlocutor profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setInterlocutorName(){
        database.collection("users")
                .document(profileID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            interlocutorUsernameTV.setText("@"+document.get("username"));
                            interlocutorNameTV.setText(String.valueOf(document.get("name")));
                        }
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });
    }
    private void setMessagesRV(){
        adapter = new MessageAdapter(getApplicationContext(), messages, email, profileID);
        messagesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, true));
        messagesRV.setAdapter(adapter);
    }
    private void getChatReference(){
        database.collection("chats")
                .whereArrayContains("interlocutors", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots){
                            if(document.exists()){
                                getMessages(document.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at getting chat reference", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void getMessages(String chatReference) {
        database.collection("messages")
                .whereEqualTo("chat", chatReference)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.exists()) {
                                messages.add(new Message(
                                        document.getId(),
                                        String.valueOf(document.get("content")),
                                        String.valueOf(document.get("sender"))
                                ));
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), document.getId(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "failed at getting messages", Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    public void onBackPressed() {
        if(from.equals(StaticClass.PROFILE_ACTIVITY)) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, profileID));
        }else if(from.equals(StaticClass.CHATS_FRAGMENT)) {
            startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, profileID)
                    .putExtra(StaticClass.TO, StaticClass.CHATS_FRAGMENT));
        }
    }
}
