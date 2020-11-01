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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.GridRVAdapter;
import com.example.openreden.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ExploreFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private ProgressBar progressBar;
    private TextView cityTV;
    private RecyclerView gridRV;
    private GridRVAdapter adapter;
    private ArrayList<User> users = new ArrayList<>();
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String email, city;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_explore, container, false);
        context = fragmentView.getContext();
        getInstances();
        findViewsByIds();
        setGridRV();
        getProfiles();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        city = sharedPreferences.getString(StaticClass.CITY, "no city");
    }
    private void findViewsByIds(){
        progressBar = fragmentView.findViewById(R.id.progressBar);
        cityTV = fragmentView.findViewById(R.id.cityTV);
        cityTV.setText(city);
        gridRV = fragmentView.findViewById(R.id.gridRV);
    }
    private void setGridRV(){
        adapter = new GridRVAdapter(context, users);
        gridRV.setLayoutManager(new GridLayoutManager(context, 3));
        gridRV.setAdapter(adapter);
    }
    private void getProfiles(){
        database.collection("users")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot document: queryDocumentSnapshots){
                    if(document.exists() && !document.getId().equals(email)){
                        setDocumentProfile(document);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setDocumentProfile(final DocumentSnapshot document){
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(document.getId() + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setUser(bytes,
                        String.valueOf(document.get("name")),
                        String.valueOf(document.get("username")));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setUser(byte[] photoBytes, String name, String username){
        User user = new User();
        //user.setPhotoBytes(photoBytes);
        user.setName(name);
        user.setUsername(username);
        users.add(user);
        adapter.notifyDataSetChanged();
    }
}
