package com.example.openreden.adapter;

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
import com.example.openreden.activity.core.MessagesActivity;
import com.example.openreden.model.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Chat> chatsList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public ChatsAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.chatsList = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Chat chat = chatsList.get(position);
        getInterlocutorID(holder, chat);
        setInterlocutorPhoto(holder);
        setInterlocutorName(holder);
        setLastMessageContent(holder, chat);
        setLastMessageTime(holder, chat);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MessagesActivity.class)
                        .putExtra(StaticClass.PROFILE_ID, holder.interlocutorID)
                        .putExtra(StaticClass.FROM, StaticClass.CHATS_FRAGMENT));
            }
        });
    }
    private void getInterlocutorID(ViewHolder holder, Chat chat){
        for(String id: chat.getInterlocutors()){
            if(!id.equals(holder.email)){
                holder.interlocutorID = id;
            }
        }
    }
    private void setInterlocutorPhoto(final ViewHolder holder){
        final long ONE_MEGABYTE = 1024 * 1024;
        holder.storage.getReference(holder.interlocutorID + StaticClass.PROFILE_PHOTO)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.interlocutorIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, holder.interlocutorIV.getWidth(),
                        holder.interlocutorIV.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failed at getting interlocutor photo", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setInterlocutorName(final ViewHolder holder){
        holder.database.collection("users")
                .document(holder.interlocutorID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            holder.interlocutorTV.setText(String.valueOf(document.get("name")));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed at getting interlocutor name", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void setLastMessageContent(ViewHolder holder, Chat chat){
        StringBuilder message;
        if(chat.getLastMessageContent().length()>20){
            message = new StringBuilder(chat.getLastMessageContent().substring(0, 20));
            message.append("...");
        }else{
            message = new StringBuilder(chat.getLastMessageContent());
        }
        holder.lastMessageContentTV.setText(message);
    }
    private void setLastMessageTime(ViewHolder holder, Chat chat){
        long messageTime = chat.getLastMessageTime();
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - messageTime;
        String time;
        Toast.makeText(context, "difference: "+messageTime, Toast.LENGTH_LONG).show();
        if(difference < 60000){ // less than a minute
            time = "now";
        }else if(difference < 3600000){ // less than an hour
            long minutes = difference/60000;
            time = minutes+" Min";
        }else if(difference < 86400000){ // less than a day
            long hours = difference/3600000;
            time = hours+" H";
        }else{
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(messageTime);
            int messageYear = c.get(Calendar.YEAR);
            c.setTimeInMillis(currentTime);
            int currentYear = c.get(Calendar.YEAR);
            if(messageYear == currentYear){
                time = new SimpleDateFormat("dd MMM").format(new Date(messageTime));
            }else{
                time = new SimpleDateFormat("dd MMM yyyy").format(new Date(messageTime));
            }
        }
        holder.lastMessageTimeTV.setText(time);
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView interlocutorIV;
        private TextView interlocutorTV, lastMessageContentTV, lastMessageTimeTV;
        private LinearLayout parentLayout;
        private View itemView;
        private FirebaseStorage storage;
        private FirebaseFirestore database;
        private String email, interlocutorID;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            getInstances();
            findViewsByIds();
            itemView.setOnClickListener(this);
        }
        void getInstances(){
            database = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            email = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(StaticClass.EMAIL, "email");
        }
        void findViewsByIds(){
            interlocutorIV = itemView.findViewById(R.id.interlocutorIV);
            interlocutorTV = itemView.findViewById(R.id.interlocutorTV);
            lastMessageContentTV = itemView.findViewById(R.id.lastMessageContentTV);
            lastMessageTimeTV = itemView.findViewById(R.id.lastMessageTimeTV);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }



        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

        }
    }


    Chat getItem(int id) {
        return chatsList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;

    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}