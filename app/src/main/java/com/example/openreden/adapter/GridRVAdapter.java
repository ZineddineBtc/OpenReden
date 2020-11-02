package com.example.openreden.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.activity.core.ProfileActivity;
import com.example.openreden.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class GridRVAdapter extends RecyclerView.Adapter<GridRVAdapter.ViewHolder> {

    private List<User> usersList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public GridRVAdapter(Context context, List<User> data) {
        this.mInflater = LayoutInflater.from(context);
        this.usersList = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_cell, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final User user = usersList.get(position);
        setProfilePhoto(holder, user);
        holder.nameTV.setText(user.getName());
        holder.usernameTV.setText("@"+user.getUsername());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, user.getId())
                        .putExtra(StaticClass.FROM, StaticClass.EXPLORE_FRAGMENT));
            }
        });
    }
    private void setProfilePhoto(final ViewHolder holder, User user){
        final long ONE_MEGABYTE = 1024 * 1024 * 20;
        holder.storage.getReference(user.getId() + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToPhoto(bytes, holder);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToPhoto(byte[] bytes, ViewHolder holder){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        holder.photoIV.setImageBitmap(
                Bitmap.createScaledBitmap(bmp, holder.photoIV.getWidth(),
                holder.photoIV.getHeight(), false)
        );
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoIV;
        private TextView nameTV, usernameTV;
        private LinearLayout parentLayout;
        private View itemView;
        private FirebaseStorage storage;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            storage = FirebaseStorage.getInstance();
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        void findViewsByIds(){
            photoIV = itemView.findViewById(R.id.photoIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            usernameTV = itemView.findViewById(R.id.usernameTV);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }



        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    User getItem(int id) {
        return usersList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}