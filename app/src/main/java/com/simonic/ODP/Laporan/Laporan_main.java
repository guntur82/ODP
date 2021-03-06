package com.simonic.ODP.Laporan;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.simonic.ODP.Laporan.Checkup.CheckupAdapter;
import com.simonic.ODP.Laporan.Checkup.Checkup_gs;
import com.simonic.ODP.Laporan.Checkup.Input_checkup;
import com.simonic.ODP.Laporan.Checkup.Lcheckup_gs;
import com.simonic.ODP.Laporan.Masalah.Input_masalah;
import com.simonic.ODP.Laporan.Suhu.Input_suhu;
import com.simonic.ODP.MainActivity;
import com.simonic.ODP.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

//https://stackoverflow.com/questions/30699302/android-design-support-library-expandable-floating-action-buttonfab-menu
//https://github.com/ajay-dewari/FloatingActionButton-Menu/blob/master/app/src/main/res/layout/activity_main.xml
//https://github.com/ajay-dewari/FloatingActionButton-Menu/blob/master/app/src/main/java/com/ajaysinghdewari/floatingactionbuttonmenu/activities/Laporan_main.java
public class Laporan_main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;
    CheckupAdapter adapter;
    NavigationView navigationView;
    FloatingActionButton fab,fab1,fab2,fab3;
    LinearLayout fab1l,fab2l,fab3l;
    View fabBGLayout;
    TextView id;
    String GetUserID;
    boolean isFABOpen = false;
    private ArrayList<Checkup_gs> reportlist = new ArrayList<>();
    private ArrayList<Lcheckup_gs> reportlist2 = new ArrayList<>();
    private ShimmerRecyclerView Rv_reportc;
    private DatabaseReference reference;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth auth;
    TelephonyManager telephonyManager;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    ArrayList<String> categories = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laporan_checkup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer);
        Rv_reportc = findViewById(R.id.rv_reportc);
        id = findViewById(R.id.deviceid);
        auth = FirebaseAuth.getInstance();
        Rv_reportc.setHasFixedSize(true);
        //CheckupAdapter adapter = new CheckupAdapter(Laporan_main.this, reportlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Laporan_main.this);
        Rv_reportc.setLayoutManager(layoutManager);
        Rv_reportc.showShimmerAdapter();

        fab1l = findViewById(R.id.ln_fab1);
        fab2l = findViewById(R.id.ln_fab2);
        fab3l = findViewById(R.id.ln_fab3);
        fab = findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fabBGLayout = findViewById(R.id.fabBGLayout);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Laporan_main.this, Input_masalah.class);
                startActivity(intent);
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Laporan_main.this, Input_checkup.class);
                startActivity(intent);
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Laporan_main.this, Input_suhu.class);
                startActivity(intent);
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(0);
        getdevice();
        getdata();
    }
    private void showFABMenu(){
        isFABOpen=true;
        fab1l.setVisibility(View.VISIBLE);
        fab2l.setVisibility(View.VISIBLE);
        fab3l.setVisibility(View.VISIBLE);
        //fabBGLayout.setVisibility(View.VISIBLE);
        fab1l.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2l.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab3l.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabBGLayout.setVisibility(View.GONE);

        fab1l.animate().translationY(0);
        fab2l.animate().translationY(0);
        fab3l.animate().translationY(0);
        fab3l.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    fab1l.setVisibility(View.GONE);
                    fab2l.setVisibility(View.GONE);
                    fab3l.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isFABOpen) {
            closeFABMenu();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.chekup:
                Toast.makeText(this,"checkup",Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
    public void getdata() {

        reference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String id2 = id.getText().toString();
        DatabaseReference quizRef = reference.child("Data ODP").child(id2).child("laporan_checkup");
        String key = quizRef.push().getKey();
        reference.child("Data ODP").child(id2).child("laporan_checkup").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportlist = new ArrayList<>();

                    Checkup_gs individu = snapshot.getValue(Checkup_gs.class);
                    reportlist.add(individu);

                    adapter = new CheckupAdapter(Laporan_main.this, reportlist);


                    Rv_reportc.setAdapter(adapter);
                    adapter.notifyDataSetChanged();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                DynamicToast.makeError(Laporan_main.this, "eror");
            }
        });
        /*FirebaseRecyclerOptions<Individu> options=
                new FirebaseRecyclerOptions.Builder<Individu>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Data ODP").child(auth.getUid()),Individu.class)
                        .build();

        adapter=new IndividuAdapter(options);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Informasi.this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);*/
    }
    public void getdevice(){
        telephonyManager  = (TelephonyManager) getSystemService(Context.
                TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(Laporan_main.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                if (!Laporan_main.this.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Laporan_main.this);
                    builder.setMessage("Please grant Phoone access so this app can detect Device ID.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @TargetApi(23)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                                    PERMISSION_REQUEST_BACKGROUND_LOCATION);
                        }

                    });
                    builder.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Laporan_main.this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }


        }
        @SuppressLint("HardwareIds") String deviceId = telephonyManager.getDeviceId();

        id.setText(deviceId);
        if (null != telephonyManager) {
            deviceId = telephonyManager.getDeviceId();
            id.setText(deviceId);

        }
        if (null == deviceId || 0 == deviceId.length()) {
            deviceId = Settings.Secure.getString(Laporan_main.this.getContentResolver(), Settings.Secure.ANDROID_ID);
            id.setText(deviceId);
        }
    }

    }

