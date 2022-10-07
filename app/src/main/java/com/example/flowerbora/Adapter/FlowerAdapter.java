package com.example.flowerbora.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.ViewHolder> {
    String TAG = "RecyclerViewAdapter";

    private List<Flower> flowers;
    private Context mContext = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView flower_image;

        ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            flower_image = itemView.findViewById(R.id.image);

        }
    }

    public FlowerAdapter(Context context, ArrayList<Flower> flowers) {
        this.mContext = context;
        this.flowers = flowers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flower, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewholder, final int position) {
        String flower_name = flowers.get(position).getName();
        viewholder.name.setText(flower_name);

        //viewholder.flower_image.setPadding(0, 0, 0, 0);
        storageReference.child("photo/" + flower_name + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(mContext)
                        .load(uri).into(viewholder.flower_image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("###", "FlowerAdapter onBindViewHolder error");
            }
        });

        //else Log.e("###", "viewHolder.flower_image = null");
    }

    @Override
    public int getItemCount() {
        if (flowers == null) return 0;
        return flowers.size();
    }
}
