package com.example.openreden.activity.core.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.ChatsAdapter;
import com.example.openreden.model.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private TextView emptyChatsListTV;
    private ProgressBar progressBar;
    private RecyclerView chatsRV;
    private ArrayList<Chat> chatsList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;
    private boolean isAdapterSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        context = fragmentView.getContext();
        getInstances();
        findViewsByIds();
        getChatsList();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
    }
    private void findViewsByIds(){
        emptyChatsListTV = fragmentView.findViewById(R.id.emptyChatsListTV);
        progressBar = fragmentView.findViewById(R.id.progressBar);
        chatsRV = fragmentView.findViewById(R.id.chatsRV);
    }
    private void setChatsRV(){
        ChatsAdapter adapter = new ChatsAdapter(context, chatsList);
        chatsRV.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        chatsRV.setAdapter(adapter);
        isAdapterSet = true;
    }
    private void getChatsList(){
        database.collection("chats")
                .whereArrayContains("interlocutors", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                            if(document.exists()){
                                chatsList.add(new Chat(
                                        document.getId(),
                                        (ArrayList<String>) document.get("interlocutors"),
                                        String.valueOf(document.get("last-message-content")),
                                        (long) document.get("last-message-time")
                                ));
                                if(!isAdapterSet) {
                                    setChatsRV();
                                }
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
