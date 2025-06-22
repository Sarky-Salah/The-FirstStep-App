package com.example.thefirststep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;
import com.google.firebase.database.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private static final int ENTRY_REQUEST_CODE = 1;
    FloatingActionButton  goToEntryButton;
    private RecyclerView recyclerView;
    private BeneficiaryAdapter adapter;
    private ArrayList<Beneficiary> beneficiaryList = new ArrayList<>();

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase setup
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("beneficiary");

        // UI references
        goToEntryButton = findViewById(R.id.goToEntryButton);
        recyclerView = findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BeneficiaryAdapter(beneficiaryList, this);
        recyclerView.setAdapter(adapter);

        // Set up Toolbar and Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // ✅ Properly initialize the toggle AFTER setSupportActionBar
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();  // this won't crash now

        // Navigation menu handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.refreshButton) {
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
                loadBeneficiaries();
            } else if (id == R.id.shareAppButton) {
                try {
                    String appPath = getPackageManager().getApplicationInfo(getPackageName(), 0).sourceDir;
                    File apkFile = new File(appPath);

                    Uri apkUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".provider",
                            apkFile
                    );

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/vnd.android.package-archive");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(shareIntent, "Share App APK via"));
                } catch (Exception e) {
                    Toast.makeText(this, "Sharing failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            else if (id == R.id.nav_exit) {
                finish(); // or System.exit(0);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        loadBeneficiaries();

        FloatingActionButton goToEntryButton = findViewById(R.id.goToEntryButton);
        goToEntryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EntryActivity.class);
            startActivityForResult(intent, ENTRY_REQUEST_CODE);
        });

        TextView footer = new TextView(this);
        footer.setText("© 2025 The-One Developers");
        footer.setTextColor(getResources().getColor(android.R.color.darker_gray));
        footer.setTextSize(12);
        footer.setPadding(20, 60, 20, 20);
        footer.setGravity(Gravity.CENTER);

        NavigationView navView = findViewById(R.id.navigationView);
        navView.addView(footer);

    }

    private void loadBeneficiaries() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                beneficiaryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Beneficiary beneficiary = dataSnapshot.getValue(Beneficiary.class);
                    if (beneficiary != null) {
                        beneficiaryList.add(beneficiary);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        loadBeneficiaries();
    }
}
