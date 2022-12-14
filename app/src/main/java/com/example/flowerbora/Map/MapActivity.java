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
import com.example.flowerbora.Adapter.FlowerAdapter;
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

    // onRequestPermissionResult ?????? ????????? ???????????? ActivityCompat.requestPermissions ??? ????????? ????????? ????????? ???????????? ?????? ??????
    private static final int PERMISSION_REQUEST_CODE = 100;
    boolean needRequest = false;

    // ?????? ???????????? ?????? ????????? ???????????? ???????????????.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;// SnackBar ??? ???????????? ???????????? View ??? ???????????????.

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
        preview = findViewById(R.id.preview);
        pre_image = findViewById(R.id.pre_image);
        pre_name = findViewById(R.id.pre_name);
        pre_period = findViewById(R.id.pre_period);
        position = findViewById(R.id.position);

        recyclerView = findViewById(R.id.map_recycler);
        mapAdapter= new MapAdapter(getApplicationContext(), null);
        recyclerView.setAdapter(mapAdapter);
        mapAdapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(MapActivity.this, RecyclerView.VERTICAL, false));

        upDateData();

        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreviewPosDown();
                drawerLayout.openDrawer(drawerView);
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

        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // ??????????????? ???????????? ????????? ?????????
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        // ?????? ?????? ?????? ??????
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        // ?????? ????????? ??????????????? ?????????
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        // SupportMapFragment ??? GoogleMap ????????? ?????? ????????? ???????????? ???????????????.
        // ??????????????? ???????????? ??????
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServiceStatus()) {
                    if (checkLocationServiceStatus()) {
                        Log.e("###", "onActivityResult : GPS ????????? ?????????");
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
            Log.e("###", "???????????? ?????? ??????");
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
        setDefaultLocation();

        flowerName = select_data.getName();

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
                        Log.e("###", "????????? ???????????? ??????");
                    }
                });

                setPreviewPosUp();
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.e("###", "onMarkerClicked");

        return false;
    }

    void setPreviewPosUp() {
        int gap = position.getBottom() - preview.getBottom();
        Log.e(TAG, "setDetailPostUp : ??????" + (gap));

        pre_image.setImageResource(R.drawable.ic_flower);
        preview.animate().translationY(gap);
        position.animate().translationY(gap);
    }

    void setPreviewPosDown() {
        Log.e(TAG, "setDetailPostDown : ??????");
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
                    Log.e("###", "???????????? : " + task.getResult().getDocuments().size());
                    for (DocumentSnapshot snap : task.getResult().getDocuments()) {
                        flowers.add(snap.toObject(Flower.class));
                    }

                    flowerList.setFlowers(flowers);
                    mapAdapter.setFlowers(flowerList.getFlowers());
                    mapAdapter.notifyDataSetChanged();
                    }

            }
        });
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() { ///drawer ???????????? ??? ?????????
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

    // ?????? ????????? ???????????? OnMapReadyCallBack ?????????????????? ????????? ???????????? getMapAsync() ????????? ???????????? ??????.
    // ????????? ?????? OnMapReady() ????????? ???????????? ??????????????? ??????????????? GoogleMap ????????? ?????????.
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        Log.e("###", "2. onMapReady");

        // ????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ????????? ?????? ????????? ??????????????? ????????? it-2???????????? ??????
        setDefaultLocation();

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // ?????? ???????????? ????????? ?????????
            startLocationUpdates();// ?????? ???????????? ??????
        } else {
            // ???????????? ???????????? ????????? ?????? ?????? ??????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                    }
                }).show();
            } else {
                // ????????? ??????
                // ??????????????? onRequestPermissionResult ?????? ?????????.
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

        // ?????? ?????? ????????? ??????????????? ???????????? ??????, ??????, ??????/?????? ????????? ???????????? CameraUpdate ??? ??????
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    // ??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    // ActivityCompat.requestPermissions ??? ????????? ????????? ????????? ????????? ???????????? ?????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ?????? ????????????
            boolean check_result = true;
            // ?????? ???????????? ??????????????? ??????
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                // ?????? ???????????? ??????
                startLocationUpdates();
            } else {
                // ????????? ???????????? ?????? ?????? ?????? ????????? ??? ?????? ????????? ????????? ?????? ?????? ??????
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ????????? ?????????.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("??????", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            }).show();
                } else {
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????.",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    // GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n?????? ????????? ??????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSSettingIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivityForResult(callGPSSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }


}