package com.example.flowerbora.Adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerbora.Class.Flower;

import java.util.ArrayList;

public class FlowerAdapter extends RecyclerView.Adapter {
    String TAG = "RecyclerViewAdapter";

    ArrayList<Flower> flowers;
    Context context;

    public FlowerAdapter(Context context, ArrayList<Flower> flowers){
        this.context=context;
        this.flowers=flowers;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return flowers.size();
    }
}
