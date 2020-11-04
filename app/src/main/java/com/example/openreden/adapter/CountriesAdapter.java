package com.example.openreden.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.activity.core.fragment.ProfileFragment;
import com.example.openreden.activity.entry.SetProfileActivity;
import com.example.openreden.model.Message;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.ViewHolder> {

    private List<String> countriesList, copyList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String country;
    private DocumentReference document;
    private SharedPreferences.Editor editor;
    private String initializedIn;

    public CountriesAdapter(Context context, List<String> data, String country,
                            DocumentReference document, SharedPreferences.Editor editor,
                            String initializedIn) {
        this.mInflater = LayoutInflater.from(context);
        this.countriesList = data;
        copyList = new ArrayList<>(countriesList);
        this.context = context;
        this.country = country;
        this.document = document;
        this.editor = editor;
        this.initializedIn = initializedIn;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.country_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.countryTV.setText(countriesList.get(position));
        if(initializedIn.equals(StaticClass.PROFILE_FRAGMENT)) {
            if (countriesList.get(position).equals(country)) {
                holder.countryTV.setTextColor(context.getColor(R.color.special));
                holder.locationIV.setVisibility(View.VISIBLE);
            }else{
                holder.countryTV.setTextColor(context.getColor(R.color.black));
                holder.locationIV.setVisibility(View.GONE);
            }
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment.shadeLL.setVisibility(View.GONE);
                    ProfileFragment.countriesLL.setVisibility(View.GONE);
                    ProfileFragment.countriesListShown = false;
                    String newCountry = countriesList.get(position);
                    ProfileFragment.countryTV.setText(newCountry);
                    document.update("country", newCountry);
                    editor.putString(StaticClass.COUNTRY, newCountry);
                    editor.apply();
                    Toast.makeText(context,
                            "Country updated",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }else if(initializedIn.equals(StaticClass.SET_PROFILE_ACTIVITY)){
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetProfileActivity.shadeLL.setVisibility(View.GONE);
                    SetProfileActivity.countriesLL.setVisibility(View.GONE);
                    SetProfileActivity.countryTV.setText(countriesList.get(position));
                    SetProfileActivity.countriesLLShown = false;
                    SetProfileActivity.countryPicked = true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return countriesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout parentLayout;
        private ImageView locationIV;
        private TextView countryTV;

        ViewHolder(final View itemView) {
            super(itemView);

            parentLayout = itemView.findViewById(R.id.parentLayout);
            locationIV = itemView.findViewById(R.id.locationIV);
            countryTV = itemView.findViewById(R.id.countryTV);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    String getItem(int id) {
        return countriesList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void filter(String queryText) {
        countriesList.clear();
        if(queryText.isEmpty()) {
            countriesList.addAll(copyList);
        }else{
            for(String text: copyList) {
                if(text.toLowerCase().contains(queryText.toLowerCase())) {
                    countriesList.add(text);
                }
            }
        }
        notifyDataSetChanged();
    }
}