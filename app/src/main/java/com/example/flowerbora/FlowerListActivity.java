package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.example.flowerbora.Adapter.FlowerAdapter;
import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.Class.FlowerList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FlowerListActivity extends AppCompatActivity implements FlowerAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private String name;
    private ImageView imageView;
    private ArrayList<Flower> flowers;
    private RecyclerView recyclerView;

    FlowerList flowerList = FlowerList.getInstance();
    FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    FlowerAdapter flowerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_list);

        flowerAdapter = new FlowerAdapter(getApplicationContext(), null);
        recyclerView = findViewById(R.id.flower_recycler);
        recyclerView.setAdapter(flowerAdapter);
        flowerAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        upDateData();
    }


    public void upDateData() {
        flowers = new ArrayList<>();
        mStore.collection("flower").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task != null) {
                    flowers.clear();
                    Log.e("###", "쿼리갯수 : " + task.getResult().getDocuments().size());
                    for (DocumentSnapshot snap : task.getResult().getDocuments()) {
                        flowers.add(snap.toObject(Flower.class));
                    }
                }

                flowerList.setFlowers(flowers);
                flowerAdapter.setFlowers(flowerList.getFlowers());
                flowerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getApplicationContext(), FlowerExplain.class);
        intent.putExtra("select_data", flowerList.getFlowers().get(position));
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        flowerAdapter.setFlowers(flowerList.getFlowers());
        flowerAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(flowerAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}