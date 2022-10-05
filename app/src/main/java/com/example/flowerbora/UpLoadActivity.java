package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.index.qual.LengthOf;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class UpLoadActivity extends AppCompatActivity {
    private final int GALLERY_CODE = 10;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private Uri file;
    private ImageView imageView;
    private String name;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_up_load);

        Button btn_image = (Button) findViewById(R.id.btn_image);
        Button btn_upload = (Button) findViewById(R.id.btn_upload);
        imageView = findViewById(R.id.imageView);
        EditText textView = (EditText) findViewById(R.id.text_name);
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://flowerbora-90ccf.appspot.com/");

        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("###", "btn_image clicked");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = textView.getText().toString();
                UpLoadPhoto();
            }
        });
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
        /*StorageReference storageReference = storage.getReference();
        StorageReference riversReference = storageReference.child("photo/" + file + ".png");
        UploadTask uploadTask = riversReference.putFile(file);

        try {
            InputStream in = getContentResolver().openInputStream(file);
            Bitmap img = BitmapFactory.decodeStream(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

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
            }
        });
    }
}