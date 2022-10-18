package com.example.flowerbora.Developer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UpLoadActivity extends AppCompatActivity implements View.OnClickListener {
    private final int GALLERY_CODE = 10;
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;
    private Uri file;
    private Flower newFlower;
    StorageReference storageReference;

    private ImageView imageView;
    private String name;
    private Button btn_image, btn_upload;
    private EditText text_name, text_feature, text_period, text_floriography, text_etc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_load);

        Constructor();
        set_clickListener();

        newFlower = new Flower();
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://flowerbora-90ccf.appspot.com/");

    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("###", "onActivityResult executed");
        Log.e("###", requestCode + " " + resultCode);
        Log.e("###", String.valueOf(file));
        if (requestCode == GALLERY_CODE) {
            file = data.getData();
            imageView.setImageURI(file);
        }

    }

    private void UpLoadPhoto() {
        StorageReference ref = storageReference.child("photo/" + name + ".jpg");

        UploadTask uploadTask;
        uploadTask = ref.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("###", "upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.e("###", "upload success");
                Toast.makeText(UpLoadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_image:
                Log.e("###", "btn_image clicked");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
                break;
            case R.id.btn_upload:
                name = text_name.getText().toString();

                newFlower.setName(name);
                newFlower.setFeature(text_feature.getText().toString());
                newFlower.setPeriod(text_period.getText().toString());
                newFlower.setFloriography(text_floriography.getText().toString());
                newFlower.setEtc(text_etc.getText().toString());

                mStore.collection("flower").document(name)
                        .set(newFlower)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.e("###", "flower 업로드 성공");
                            }
                        });
                UpLoadPhoto();
                break;
        }
    }

    public void Constructor() {
        btn_image = findViewById(R.id.btn_image);
        btn_upload = findViewById(R.id.btn_upload);
        imageView = findViewById(R.id.imageView);
        text_name = findViewById(R.id.text_name);
        text_feature = findViewById(R.id.text_feature);
        text_floriography = findViewById(R.id.text_floriography);
        text_period = findViewById(R.id.text_period);
        text_etc = findViewById(R.id.text_etc);
    }

    public void set_clickListener() {
        btn_image.setOnClickListener(this);
        btn_upload.setOnClickListener(this);
    }
}