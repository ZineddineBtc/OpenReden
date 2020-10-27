package com.example.openreden.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openreden.R;
import com.example.openreden.StaticClass;
import com.example.openreden.model.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private List<Chat> chatsList, copyList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private int position;

    public ChatsAdapter(Context context, List<Chat> data) {
        this.mInflater = LayoutInflater.from(context);
        this.chatsList = data;
        this.context = context;
        copyList = new ArrayList<>(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        this.position = position;
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView interlocutorIV;
        private TextView interlocutorTV, lastMessageContentTV, lastMessageTimeTV;
        private View itemView;
        private FirebaseStorage storage;
        private FirebaseFirestore database;
        private Chat chat;
        private String email, interlocutorID;

        ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            getInstances();
            findViewsByIds();
            setData();
            itemView.setOnClickListener(this);
        }
        void getInstances(){
            database = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
            email = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(StaticClass.EMAIL, "email");
            chat = chatsList.get(position);
            getInterlocutorID();
        }
        void getInterlocutorID(){
            for(String id: chat.getInterlocutors()){
                if(!id.equals(email)){
                    interlocutorID = id;
                }
            }
        }
        void findViewsByIds(){
            interlocutorIV = itemView.findViewById(R.id.interlocutorIV);
            interlocutorTV = itemView.findViewById(R.id.interlocutorTV);
            lastMessageContentTV = itemView.findViewById(R.id.lastMessageContentTV);
            lastMessageTimeTV = itemView.findViewById(R.id.lastMessageTimeTV);
        }
        void setData(){
            setInterlocutorPhoto();
            setInterlocutorName();
            setLastMessageContent();
            //lastMessageTimeTV.setText(chat.getLastMessageTime());
        }
        void setInterlocutorPhoto(){
            final long ONE_MEGABYTE = 1024 * 1024;
            storage.getReference(interlocutorID + StaticClass.PROFILE_PHOTO)
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    interlocutorIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, interlocutorIV.getWidth(),
                            interlocutorIV.getHeight(), false));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, "Failed at getting interlocutor photo", Toast.LENGTH_LONG).show();
                }
            });
        }
        void setInterlocutorName(){
            database.collection("users")
                    .document(interlocutorID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            if(document.exists()){
                                interlocutorTV.setText(String.valueOf(document.get("name")));
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
        void setLastMessageContent(){
            StringBuilder message;
            if(chat.getLastMessageContent().length()>20){
                message = new StringBuilder(chat.getLastMessageContent().substring(0, 20));
                message.append("...");
            }else{
                message = new StringBuilder(chat.getLastMessageContent());
            }
            lastMessageContentTV.setText(message);
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

    /*public void filter(String queryText) {
        chatsList.clear();
        if(queryText.isEmpty()) {
            chatsList.addAll(copyList);
        }else{
            for(Chat chat: copyList) {
                if(chat.getInterlocutor().getName().toLowerCase().contains(queryText.toLowerCase())) {
                    chatsList.add(chat);
                }
            }
        }
        notifyDataSetChanged();
    }*/
}