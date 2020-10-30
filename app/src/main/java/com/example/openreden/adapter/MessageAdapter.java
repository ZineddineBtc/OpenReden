package com.example.openreden.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
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
import com.example.openreden.model.Chat;
import com.example.openreden.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messagesList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private String userID, interlocutorID;
    private int position;

    public MessageAdapter(Context context, List<Message> data,
                          String userID, String interlocutorID) {
        this.mInflater = LayoutInflater.from(context);
        this.messagesList = data;
        this.context = context;
        this.userID = userID;
        this.interlocutorID = interlocutorID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Message message = messagesList.get(position);
        setMessage(holder, message);
    }

    private void setMessage(ViewHolder holder, Message message){
        holder.messageTV.setText(message.getContent());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if(message.getSender().equals(userID)){
            holder.messageTV.setBackgroundColor(context.getColor(R.color.special));
            holder.parentLayout.setGravity(Gravity.END);
            params.setMargins(50, 10, 10, 10);
        }else{
            holder.messageTV.setBackgroundColor(context.getColor(R.color.grey));
            holder.parentLayout.setGravity(Gravity.START);
            params.setMargins(10, 10, 50, 10);
        }
        holder.parentLayout.setLayoutParams(params);


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout parentLayout;
        private TextView messageTV;
        private View itemView;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        void findViewsByIds(){
            parentLayout = itemView.findViewById(R.id.parentLayout);
            messageTV = itemView.findViewById(R.id.messageTV);
        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Message getItem(int id) {
        return messagesList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}