package com.example.openreden.activity.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.lang.System;

public class MessagesActivity extends AppCompatActivity {

    private ImageView interlocutorPhotoIV;
    private TextView interlocutorUsernameTV, interlocutorNameTV, textET;
    private RecyclerView messagesRV;
    private MessageAdapter adapter;
    private ArrayList<Message> messages = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private DocumentReference messageReference;
    private Map<String, Object> messageMap = new HashMap<>(), chatMap = new HashMap<>();
    private Message message = new Message();
    private String email, interlocutorID, from, chatReference, content,
            fetched = "-fetched", emailFetched, interlocutorFetched;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        getInstances();
        findViewsByIds();
        setInterlocutorPhoto();
        setMessagesRV();
        getChatReference();
    }
    private void getInstances(){
        Objects.requireNonNull(getSupportActionBar()).hide();
        interlocutorID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        interlocutorFetched = Objects.requireNonNull(interlocutorID)
                .replace(".", "-")+fetched;
        from = getIntent().getStringExtra(StaticClass.FROM);
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        emailFetched = Objects.requireNonNull(email)
                .replace(".", "-")+fetched;
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
    }
    private void findViewsByIds(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView backIV = toolbar.findViewById(R.id.backIV);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        textET = findViewById(R.id.textET);
        ImageView sendTextIV = findViewById(R.id.sendTextIV);
        sendTextIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = textET.getText().toString().trim();
                if (!content.isEmpty()) send();
            }
        });
        interlocutorPhotoIV = toolbar.findViewById(R.id.interlocutorPhotoIV);
        interlocutorUsernameTV = toolbar.findViewById(R.id.interlocutorUsernameTV);
        interlocutorNameTV = toolbar.findViewById(R.id.interlocutorNameTV);
        messagesRV = findViewById(R.id.messagesRV);
    }
    private void setInterlocutorPhoto(){
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(interlocutorID + StaticClass.PROFILE_PHOTO)
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
                .document(interlocutorID)
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
        adapter = new MessageAdapter(getApplicationContext(), messages, email, interlocutorID);
        messagesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
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
                                chatReference = document.getId();
                                getMessages();
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
    private void getMessages() {
        database.collection("messages")
                .whereEqualTo("chat", chatReference)
                .orderBy("time")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.exists()) {
                                messages.add(new Message(
                                        document.getId(),
                                        String.valueOf(document.get("content")),
                                        String.valueOf(document.get("sender")),
                                        (long) document.get("time")
                                ));
                                adapter.notifyDataSetChanged();
                                boolean fetched = (boolean) document.get(emailFetched);
                                if(!fetched) setMessageFetched(document.getId());
                            }
                        }
                        setMessageListener();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", e.getMessage());
                    }
                });
    }
    private void setMessageListener(){
        database.collection("messages")
                .whereEqualTo(emailFetched, false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }else{
                            for(QueryDocumentSnapshot document: value) {
                                if (document != null && document.exists()) {
                                    messages.add(new Message(
                                            document.getId(),
                                            String.valueOf(document.get("content")),
                                            String.valueOf(document.get("sender")),
                                            (long) document.get("time")
                                    ));
                                    adapter.notifyDataSetChanged();
                                    setMessageFetched(document.getId());
                                }
                            }
                        }
                    }
                });
    }
    private void setMessageFetched(String messageID){
        database.collection("messages")
                .document(messageID)
                .update(email, true);
    }
    private void setMessage(){
        message.setContent(content);
        message.setSender(email);
        message.setTime(System.currentTimeMillis());
    }
    private void putMessageMap(){
        messageMap.put("content", message.getContent());
        messageMap.put("sender", message.getSender());
        messageMap.put("receiver", interlocutorID);
        messageMap.put("chat", chatReference);
        messageMap.put("time", message.getTime());
        messageMap.put(emailFetched, true);
        messageMap.put(interlocutorFetched, false);
    }
    private void send(){
        setMessage();
        putMessageMap();
        messageReference = database.collection("messages").document();
        messageReference.set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messages.add(message);
                        adapter.notifyDataSetChanged();
                        textET.setText("");
                        updateChatDB();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error sending message",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void putChatMap(){
        chatMap.put("last-message-content", message.getContent());
        chatMap.put("sender", message.getSender());
        chatMap.put("messages", messageReference.getId());
        chatMap.put("last-message-time", message.getTime());
    }
    private void updateChatDB(){
        putChatMap();
        database.collection("chats")
                .document(chatReference)
                .update(chatMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Error updating chatDB",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onBackPressed() {
        if(from.equals(StaticClass.PROFILE_ACTIVITY)) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, interlocutorID));
        }else if(from.equals(StaticClass.CHATS_FRAGMENT)) {
            startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                    .putExtra(StaticClass.PROFILE_ID, interlocutorID)
                    .putExtra(StaticClass.TO, StaticClass.CHATS_FRAGMENT));
        }
    }
}
