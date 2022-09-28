package com.example.flowerbora;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {
    ImageView imageView;
    CameraSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        surfaceView = findViewById(R.id.surfaceView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카메라 사진 캡쳐

                capture();
            }

        });
    }

    @Override   //카메라 권한 함수
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 101:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "카메라 권한 사용자가 승인함",Toast.LENGTH_LONG).show();
                    }
                    else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this, "카메라 권한 사용자가 허용하지 않음.",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(this, "수신권한 부여받지 못함.",Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

    public void capture(){
        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //bytearray 형식으로 전달
                //이걸이용해서 이미지뷰로 보여주거나 파일로 저장
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8; // 1/8사이즈로 보여주기
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); //data 어레이 안에 있는 데이터 불러와서 비트맵에 저장
                Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                intent.putExtra("사진", bitmap);
                startActivity(intent);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int newWidth = 200;
                int newHeight = 200;

                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;

                Matrix matrix = new Matrix();

                matrix.postScale(scaleWidth, scaleHeight);

                matrix.postRotate(90);



                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,width,height,matrix,true);
                BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);



                imageView.setImageDrawable(new BitmapDrawable(resizedBitmap));//이미지뷰에 사진 보여주기
                camera.startPreview();
            }
        });


    }
}