package com.example.flowerbora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.flowerbora.Adapter.FlowerAdapter;
import com.example.flowerbora.Class.Flower;

import java.util.ArrayList;

public class FlowerListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_list);

        FlowerAdapter adapter;
        RecyclerView recyclerView;

        ArrayList<Flower> flowers = new ArrayList<>();

        //flowers.add(new Flower(, ));

        recyclerView = findViewById(R.id.flower_recycler);
        adapter = new FlowerAdapter(FlowerListActivity.this, flowers);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(FlowerListActivity.this, RecyclerView.VERTICAL, false));
    }
}