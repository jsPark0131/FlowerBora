package com.example.flowerbora.Developer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flowerbora.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ResultActivity extends AppCompatActivity {
    TextView result, confidence;
    ImageView imageView;
    int imageSize = 224;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        float[] confidences = intent.getFloatArrayExtra("confidence");
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }
        String[] classes = {"벚꽃", "라일락", "목련", "아카시아", "배롱나무꽃", "장미", "개나리꽃", "진달래꽃", "감국", "감꽃"};
        result.setText(classes[maxPos]);
        String s = "";
        for (int i = 0; i < classes.length; i++) {
            s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
        }
        confidence.setText(s);

        String path = "photo/" + classes[maxPos] + ".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ResultActivity.this).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("###", "이미지 불러오기 실패");
            }
        });
    }
}