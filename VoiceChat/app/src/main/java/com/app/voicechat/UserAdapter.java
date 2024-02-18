package com.app.voicechat;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.voicechat.databinding.UserItemBinding;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    private ArrayList<User> userArrayList;
    private Context context;

    public UserAdapter(ArrayList<User>userArrayList, Context context){
        this.userArrayList= userArrayList;
        this.context=context;
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        UserItemBinding recyclerRowBinding;

        public MyHolder(@NonNull UserItemBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemBinding recyclerRowBinding = UserItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MyHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {


        User user = userArrayList.get(position);

        holder.recyclerRowBinding.username.setText(user.getUsername());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("user",user);
                context.startActivity(intent);
            }
        });



    }


    @Override
    public int getItemCount() {
        return null!=userArrayList?userArrayList.size():0;
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

}