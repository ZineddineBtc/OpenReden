package com.example.openreden.activity.core.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.adapter.CountriesAdapter;
import com.example.openreden.adapter.GridRVAdapter;
import com.example.openreden.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;

public class ExploreFragment extends Fragment {

    private View fragmentView;
    private Context context;
    private ProgressBar progressBar;
    private TextView countryTV;
    private LinearLayout countryLL, shadeLL, countriesLL;
    private RecyclerView gridRV;
    private GridRVAdapter gridRVAdapter;
    private ArrayList<User> users = new ArrayList<>();
    private ListView countriesLV;
    private ArrayAdapter countriesAdapter;
    private ArrayList<String> allCountries, copyList;
    private SearchView searchCountriesSV;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String email, country;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_explore, container, false);
        context = fragmentView.getContext();
        getInstances();
        findViewsByIds();
        setGridRV();
        getProfiles();
        setCountriesLV();
        return fragmentView;
    }
    private void getInstances(){
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        SharedPreferences sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        country = sharedPreferences.getString(StaticClass.COUNTRY, "no country");
        allCountries = new ArrayList<>(Arrays.asList(StaticClass.countries));
        copyList = new ArrayList<>(allCountries);
    }
    private void findViewsByIds(){
        progressBar = fragmentView.findViewById(R.id.progressBar);
        countryLL = fragmentView.findViewById(R.id.countryLL);
        countryLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shadeLL.setVisibility(View.VISIBLE);
                countriesLL.setVisibility(View.VISIBLE);
            }
        });
        countryTV = fragmentView.findViewById(R.id.countryTV);
        countryTV.setText(country);
        gridRV = fragmentView.findViewById(R.id.gridRV);
        shadeLL = fragmentView.findViewById(R.id.shadeLL);
        countriesLL = fragmentView.findViewById(R.id.countriesLL);
        searchCountriesSV = fragmentView.findViewById(R.id.searchCountrySV);
        searchCountriesSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                allCountries.clear();
                if(queryText.isEmpty()) {
                    allCountries.addAll(copyList);
                }else{
                    for(String text: copyList) {
                        if(text.toLowerCase().contains(queryText.toLowerCase())) {
                            allCountries.add(text);
                        }
                    }
                }
                countriesAdapter.notifyDataSetChanged();
                return false;
            }
        });
        countriesLV = fragmentView.findViewById(R.id.countriesLV);
    }
    private void setGridRV(){
        gridRVAdapter = new GridRVAdapter(context, users);
        gridRV.setLayoutManager(new GridLayoutManager(context, 3));
        gridRV.setAdapter(gridRVAdapter);
        gridRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                String t;
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        countryLL.setAlpha(1f);
                        for(int i = 0; i<= country.length(); i++){
                            t = country.substring(0, i);
                            countryTV.setText(t);
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        countryLL.setAlpha(0.5f);
                        for(int i = country.length()-1; i>=0; i--){
                            t = country.substring(0, i);
                            countryTV.setText(t);
                        }
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
    private void getProfiles(){
        users.clear();
        database.collection("users")
                .whereEqualTo("country", country)
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
        User user = new User();
        user.setId(document.getId());
        user.setName(String.valueOf(document.get("name")));
        user.setUsername(String.valueOf(document.get("username")));
        users.add(user);
        gridRVAdapter.notifyDataSetChanged();
    }
    private void setCountriesLV(){
        countriesAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1,
                allCountries);
        countriesLV.setAdapter(countriesAdapter);
        countriesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                country = allCountries.get(position);
                countryTV.setText(country);
                getProfiles();
                shadeLL.setVisibility(View.GONE);
                countriesLL.setVisibility(View.GONE);
            }
        });
    }
}
