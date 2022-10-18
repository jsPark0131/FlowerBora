package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flowerbora.Class.Flower;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class FlowerExplain extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore mstore = FirebaseFirestore.getInstance();
    Flower select_data;

    private String name;
    private TextView text_name, text_floriography, text_feature, text_period, text_Etc;
    private ImageView imageView;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_explain);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        select_data = (Flower) intent.getSerializableExtra("select_data");

        Constructor();
        WritingData();
        Image_Load();
    }

    public void Image_Load() {
        String path = "photo/" + name + ".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(FlowerExplain.this).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("###", "이미지 불러오기 실패");
            }
        });
    }

    public void WritingData() {
        name = select_data.getName();

        text_name.setText(name);
        text_floriography.setText(select_data.getFloriography());
        text_feature.setText(select_data.getFeature());
        text_period.setText(select_data.getPeriod());
        text_Etc.setText(select_data.getEtc());
    }

    public void Constructor() {
        text_name = findViewById(R.id.text_name);
        text_floriography = findViewById(R.id.text_floriography);
        text_feature = findViewById(R.id.text_feature);
        text_period = findViewById(R.id.text_period);
        text_Etc = findViewById(R.id.text_Etc);

        imageView = findViewById(R.id.image);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng DEFAULT_LOCATION = new LatLng(35.889950783370985, 128.61165940761563);

        mMap = googleMap;
        List<Double> p = select_data.getLatlng_double();
        if (p.size() == 0) {
            Log.e("###", "위치정보 추가 필요");
        } else {
            for (int i = 0; i < p.size(); i += 2) {
                LatLng ret = new LatLng(p.get(i), p.get(i + 1));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ret);
                markerOptions.title(select_data.getName());
                markerOptions.draggable(true);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions);
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 14.7f));
    }
}