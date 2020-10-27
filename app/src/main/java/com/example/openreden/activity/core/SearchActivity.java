package com.example.openreden.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.ChatsAdapter;
import com.example.openreden.adapter.ResultAdapter;
import com.example.openreden.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private TextView noResultsTV;
    private ProgressBar progressBar;
    private RecyclerView resultsRV;
    private ResultAdapter adapter;
    private ArrayList<User> usersList = new ArrayList<>();
    private FirebaseFirestore database;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Objects.requireNonNull(getSupportActionBar()).hide();
        database = FirebaseFirestore.getInstance();
        email = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, "no email");
        findViewsByIds();
        setResultRV();
    }
    private void findViewsByIds(){
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView backIV = toolbar.findViewById(R.id.backIV);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CoreActivity.class));
            }
        });
        noResultsTV = findViewById(R.id.noResultsTV);
        resultsRV = findViewById(R.id.resultRV);
        EditText searchET = toolbar.findViewById(R.id.searchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usersList.clear();
                adapter.notifyDataSetChanged();
                if(count>0){
                    progressBar.setVisibility(View.VISIBLE);
                    search(String.valueOf(s));
                    checkIfNoResults();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
    }
    private void setResultRV(){
        adapter = new ResultAdapter(getApplicationContext(), usersList);
        resultsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        resultsRV.setAdapter(adapter);
    }
    private void search(String input){
        database.collection("users")
                .whereGreaterThanOrEqualTo("name", input)
                .whereLessThanOrEqualTo("name", input+"\uF8FF")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document: queryDocumentSnapshots.getDocuments()){
                            if(document.getId().equals(email)) continue;
                            usersList.add(new User(
                                    document.getId(),
                                    String.valueOf(document.get("username")),
                                    String.valueOf(document.get("name"))
                            ));
                        }
                        adapter.notifyDataSetChanged();
                        noResultsTV.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed at getting results", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkIfNoResults(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(usersList.isEmpty()){
                    noResultsTV.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, 1000);
    }
}