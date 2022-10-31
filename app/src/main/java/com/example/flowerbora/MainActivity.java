package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flowerbora.Camera.CameraSurfaceView;
import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.Developer.ResultActivity;
import com.example.flowerbora.Developer.UpLoadActivity;
import com.example.flowerbora.Map.MapActivity;
import com.example.flowerbora.ml.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    CameraSurfaceView surfaceView;
    int imageSize = 250;
    static final int PERMISSION_REQUEST_CODE = 0X00001;

    FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private Flower flowerData = new Flower();
    private String flowerName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);

        onCheckCameraPermission();
        ImageView button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라 사진 캡쳐
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    capture();
                } else {
                    onCheckCameraPermission();
                }
            }
        });

        onCheckLocationPermission();
        LinearLayout map = findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                } else {
                    onCheckLocationPermission();
                }
            }
        });

        LinearLayout list = findViewById(R.id.btn_list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FlowerListActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onCheckCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "기능을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
            }
        }
    }

    public void onCheckLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "기능을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override   //카메라 권한 함수
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                Log.e("###", "onRequestPermission");
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "카메라 권한 사용자가 승인함", Toast.LENGTH_LONG).show();PackageManager packageManager = getPackageManager();
                        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                        ComponentName componentName = intent.getComponent();
                        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                        startActivity(mainIntent);
                        System.exit(0);
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "카메라 권한 사용자가 허용하지 않음.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "수신권한 부여받지 못함.", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 승인함", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "권한 부여받지 못함.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    public void capture() {
        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //bytearray 형식으로 전달
                //이걸이용해서 이미지뷰로 보여주거나 파일로 저장

                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 8; // 1/8사이즈로 보여주기

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); //data 어레이 안에 있는 데이터 불러와서 비트맵에 저장

                // 회전되어 나오는 이미지를 바로 돌려줌
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newWidth = 200;
                int newHeight = 200;
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                matrix.postRotate(90);

                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

                int dimension = Math.min(resizedBitmap.getWidth(), resizedBitmap.getHeight());
                resizedBitmap = ThumbnailUtils.extractThumbnail(resizedBitmap, dimension, dimension);
                resizedBitmap = Bitmap.createScaledBitmap(resizedBitmap, imageSize, imageSize, false);
                classifyImage(resizedBitmap);

                camera.startPreview();
            }
        });
    }

    public void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 250, 250, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // get 1D array of 224 * 224 pixels in image
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // 정확성이 가장 높은 것을 도출
            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"벚꽃", "라일락", "목련", "아카시아", "배롱나무꽃", "장미", "개나리꽃", "진달래꽃", "감국", "감꽃"};
            flowerName = classes[maxPos];

            Log.e("###", flowerName);

            Intent intent = new Intent(MainActivity.this, FlowerExplain.class);
            mStore.collection("flower").whereEqualTo("name", flowerName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    flowerData = task.getResult().getDocuments().get(0).toObject(Flower.class);

                    intent.putExtra("select_data", flowerData);
                    model.close();
                    startActivity(intent);
                }
            });

            /*Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("confidence", confidences);*/

            // Releases model resources if no longer used.

        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
}