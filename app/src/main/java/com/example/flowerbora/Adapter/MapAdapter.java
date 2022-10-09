package com.example.flowerbora.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.R;

import java.util.ArrayList;
import java.util.List;

public class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {
    public OnItemClickListener mListener = null;

    private Context mContext = null;
    private List<Flower> flowers;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    public MapAdapter(Context context, ArrayList<Flower> flowers) {
        this.mContext = context;
        this.flowers = flowers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_flower, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.name.setText(flowers.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (flowers == null) return 0;
        return flowers.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setFlowers(List<Flower> flowers) {
        this.flowers = flowers;
    }
}
