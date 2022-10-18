package com.example.flowerbora.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flowerbora.Adapter.MapAdapter;
import com.example.flowerbora.Class.Flower;
import com.example.flowerbora.Class.FlowerList;
import com.example.flowerbora.FlowerExplain;
import com.example.flowerbora.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000; // 1 second
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5 second

    // onRequestPermissionResult 에서 수신된 결과에서 ActivityCompat.requestPermissions 를 사용한 퍼미션 요청을 구별하기 위해 사용
    private static final int PERMISSION_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;// SnackBar 를 사용하기 위해서는 View 가 필요합니다.

    private DrawerLayout drawerLayout;
    private View drawerView;
    private Button btn_close;
    private ImageView btn_open;

    private ArrayList<Flower> flowers;
    private RecyclerView recyclerView;
    FlowerList flowerList = FlowerList.getInstance();
    FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    MapAdapter mapAdapter;
    Flower select_data;

    private String flowerName;
    private LinearLayout preview;
    private ImageView pre_image;
    private TextView pre_name, pre_period, position;
    private Flower flowerData = new Flower();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);

        mLayout = findViewById(R.id.layout_map);

        drawerLayout = findViewById(R.id.layout_map);
        drawerView = findViewById(R.id.drawer);
        btn_open = findViewById(R.id.btn_bar);
        btn_close = findViewById(R.id.btn_close);
        preview = findViewById(R.id.preview);
        pre_image = findViewById(R.id.pre_image);
        pre_name = findViewById(R.id.pre_name);
        pre_period = findViewById(R.id.pre_period);
        position = findViewById(R.id.position);

        recyclerView = findViewById(R.id.map_recycler);
        upDateData();

        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreviewPosDown();
                drawerLayout.openDrawer(drawerView);
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawers();
            }
        });
        drawerLayout.setDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FlowerExplain.class);
                intent.putExtra("select_data", flowerData);
                startActivity(intent);
            }
        });

        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 실시간으로 사용자의 위치를 보여줌
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        // 현재 위치 설정 받기
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        // 위치 서비스 클라이언트 만들기
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        // SupportMapFragment 는 GoogleMap 객체의 수명 주기를 관리하는 프래그먼트.
        // 프래그먼트 정적으로 추가
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServiceStatus()) {
                    if (checkLocationServiceStatus()) {
                        Log.e("###", "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        select_data = flowerList.getFlowers().get(position);
        List<Double> p = select_data.getLatlng_double();
        if (p.size() == 0) {
            Log.e("###", "위치정보 추가 필요");
        } else {
            if (currentMarker != null) {
                currentMarker.remove();
                mMap.clear();
            }
            for (int i = 0; i < p.size(); i += 2) {
                LatLng ret = new LatLng(p.get(i), p.get(i + 1));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ret);
                markerOptions.title(select_data.getName());
                markerOptions.draggable(true);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                currentMarker = mMap.addMarker(markerOptions);
            }
        }
        Log.e("###", "clicked position : " + position);
        drawerLayout.closeDrawers();

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.e("###", "onMarkerClicked");
        flowerName = marker.getTitle();

        mStore.collection("flower").whereEqualTo("name", flowerName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                flowerData = task.getResult().getDocuments().get(0).toObject(Flower.class);

                pre_name.setText(flowerName);
                pre_period.setText(flowerData.getPeriod());

                storageReference.child("photo/" + flowerName + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(MapActivity.this).load(uri).into(pre_image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("###", "이미지 불러오기 실패");
                    }
                });

                setPreviewPosUp();
            }
        });
        return false;
    }

    void setPreviewPosUp() {
        int gap = position.getBottom() - preview.getBottom();
        Log.e(TAG, "setDetailPostUp : 실행" + (gap));

        pre_image.setImageResource(R.drawable.ic_flower);
        preview.animate().translationY(gap);
        position.animate().translationY(gap);
    }

    void setPreviewPosDown() {
        Log.e(TAG, "setDetailPostDown : 실행");
        preview.animate().translationY(0);
        position.animate().translationY(0);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mapAdapter.setFlowers(flowerList.getFlowers());
        mapAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mapAdapter);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

                    flowerList.setFlowers(flowers);
                    mapAdapter = new MapAdapter(MapActivity.this, flowers);
                    recyclerView.setAdapter(mapAdapter);
                    mapAdapter.setOnItemClickListener(MapActivity.this);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MapActivity.this, RecyclerView.VERTICAL, false));
                }

            }
        });
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() { ///drawer 오픈됬을 때 작동함
        @Override
        public void onDrawerSlide(@NonNull @org.jetbrains.annotations.NotNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull @org.jetbrains.annotations.NotNull View drawerView) {
            upDateData();
        }

        @Override
        public void onDrawerClosed(@NonNull @org.jetbrains.annotations.NotNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    // 지도 객체를 얻으려면 OnMapReadyCallBack 인터페이스를 구현한 클래스를 getMapAsync() 함수를 이용하여 등록.
    // 이렇게 하면 OnMapReady() 함수가 자동으로 호출되면서 매개변수로 GoogleMap 객체가 전달됨.
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        Log.e("###", "2. onMapReady");

        // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기 전에 지도의 초기위치를 경북대 it-2호관으로 지정
        setDefaultLocation();

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 이미 퍼미션을 가지고 있다면
            startLocationUpdates();// 위치 업데이트 시작
        } else {
            // 사용자가 퍼미션을 거부한 적이 있는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 퍼미션 요청
                // 요청결과는 onRequestPermissionResult 에서 수신됨.
                ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.e("###", "onMapClick : ");
                Log.e("###", String.valueOf(latLng.latitude));
                Log.e("###", String.valueOf(latLng.longitude));
            }
        });


        mMap.setOnMarkerClickListener(this);
    }

    private void startLocationUpdates() {
        if (!checkLocationServiceStatus()) {
            Log.e("###", "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Log.e("###", "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (checkPermission()) {
            Log.e("###", "1. onStart : call mFusedLocationClient.requestLocationUpdates");

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {
            Log.e("###", "onStop : call stopLocationUpdates");
        }
    }

    public boolean checkLocationServiceStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public void setDefaultLocation() {
        LatLng DEFAULT_LOCATION = new LatLng(35.887245, 128.6117684);

        Log.e("###", "3. setDefaultLocation Done");

        // 다른 모든 속성을 유지하면서 카메라의 위도, 경도, 확대/축소 수준을 변경하는 CameraUpdate 를 제공
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    // 여기부터는 런타임 퍼미션 처리를 위한 메소드들
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    // ActivityCompat.requestPermissions 를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신 되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                // 위치 업데이트 시작
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있는 경우 앱을 사용할 수 없는 이유를 설명해 주고 앱을 종료
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해 주세요.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("확인", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            }).show();
                } else {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    // GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 수정하겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSSettingIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivityForResult(callGPSSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }


}