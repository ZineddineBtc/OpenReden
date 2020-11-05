package com.example.openreden.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private String userID;
    public MessageAdapter(Context context, List<Message> data,
                          String userID, String interlocutorID) {
        this.mInflater = LayoutInflater.from(context);
        this.messagesList = data;
        this.context = context;
        this.userID = userID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Message message = messagesList.get(position);
        setMessage(holder, message);
        holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setOnLongClickListener(holder, message);
                return false;
            }
        });
    }

    private void setMessage(ViewHolder holder, Message message){
        holder.messageTV.setText(message.getContent());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if(message.getSender().equals(userID)){
            holder.messageTV.setBackground(context.getDrawable(R.drawable.special_background_rounded_border));
            holder.parentLayout.setGravity(Gravity.END);
            params.setMargins(50, 10, 10, 10);
        }else{
            holder.messageTV.setBackground(context.getDrawable(R.drawable.grey_background_rounded_border));
            holder.parentLayout.setGravity(Gravity.START);
            params.setMargins(10, 10, 50, 10);
        }
        holder.parentLayout.setLayoutParams(params);


    }
    private void setOnLongClickListener(final ViewHolder holder, final Message message){
        new AlertDialog.Builder(context.getApplicationContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton(
                        Html.fromHtml("<font color=\"#FF0000\"> Delete </font>"),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                holder.database.collection("messages")
                                        .document(message.getId())
                                        .delete();
                            }
                        })
                .setNegativeButton(
                        Html.fromHtml("<font color=\"#28a895\"> Cancel </font>"),
                        null)
                .show();
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout parentLayout;
        private TextView messageTV;
        private View itemView;
        private FirebaseFirestore database;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            findViewsByIds();
            database = FirebaseFirestore.getInstance();

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