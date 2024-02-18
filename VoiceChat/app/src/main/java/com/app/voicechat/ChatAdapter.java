package com.app.voicechat;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.voicechat.databinding.ChatItemBinding;
import com.app.voicechat.databinding.ChatOtherItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    private ArrayList<Chat> chatArrayList;
    private Context context;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;
    private FirebaseUser firebaseUser;


    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;




    public ChatAdapter(ArrayList<Chat>chatArrayList, Context context){
        this.chatArrayList= chatArrayList;
        this.context=context;
    }

    public static class MyHolder extends RecyclerView.ViewHolder{

        public TextView time;
        public TextView username;
        public ImageView play;
        public ImageView delete;

        public MyHolder(View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.time);
            username = itemView.findViewById(R.id.username);
            play = itemView.findViewById(R.id.playBtn);
            delete = itemView.findViewById(R.id.deleteBtn);
        }

    }
    @NonNull
    @Override
    public ChatAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_other_item, parent, false);
            return new ChatAdapter.MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
            return new ChatAdapter.MyHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {


        Chat chat = chatArrayList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        holder.time.setText(convertTime(chat.getTime()));


        if (currentlyPlayingPosition == holder.getAdapterPosition()) {
            holder.play.setImageResource(R.drawable.ic_baseline_pause_24);
        } else {
            holder.play.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        }


        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                if (currentlyPlayingPosition == clickedPosition) {
                    currentlyPlayingPosition = -1;
                    stopPlaying();
                } else {
                    currentlyPlayingPosition = clickedPosition;
                    play(chatArrayList.get(clickedPosition).getUrl());
                }

                notifyDataSetChanged();


            }
        });

        if (firebaseUser.getUid().equals(chat.getSenderId())){
            holder.delete.setVisibility(View.VISIBLE);
        }else{
            holder.delete.setVisibility(View.GONE);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(chat);
            }
        });

        getUserName(chat,holder);

    }

    private void delete(Chat chat){
        //kullanıcının kendi mesajıysa ses her iki taraftan da silinir
        if (firebaseUser.getUid().equals(chat.getSenderId())){
            FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Silindi", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

    }


    private void getUserName(Chat chat,MyHolder holder){
        //Chat sınıfından senderId sayesinde gönderen kişinin bilgilerine erişilir
        FirebaseDatabase.getInstance().getReference().child("Users").child(chat.getSenderId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                holder.username.setText(user.getUsername());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void play(String sesUrl) {
        //çalan bir ses varsa önce onu durdurur
        stopPlaying();

        try {
            //daha sonra mediaplayer objesi oluşturulur ve modelden gelen url atanarak oynatılır
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(sesUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }






    private String convertTime(String time){
        //Milisaniye olarak tutulan zamanı tarih  formatına çevirir
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM k:mm");
        String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(time))));
        return dateString;
    }

    @Override
    public int getItemCount() {
        return null!=chatArrayList?chatArrayList.size():0;
    }


    @Override
    public int getItemViewType(int position) {
        //kullanıcının kendi mesajıysa farklı, karşısındakinin mesajıysa farklı tasarımlarda gösterir
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatArrayList.get(position).getSenderId().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

}