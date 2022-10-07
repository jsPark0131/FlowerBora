package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.flowerbora.Adapter.FlowerAdapter;
import com.example.flowerbora.Class.Flower;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class FlowerListActivity extends AppCompatActivity {
    private String name;
    private ImageView imageView;
    private ArrayList<Flower> flowers;
    private RecyclerView recyclerView;
    private FlowerAdapter adapter;

    FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_list);

        recyclerView = findViewById(R.id.flower_recycler);
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
                    adapter = new FlowerAdapter(FlowerListActivity.this, flowers);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(FlowerListActivity.this, RecyclerView.VERTICAL, false));
                }
            }
        });
    }
}