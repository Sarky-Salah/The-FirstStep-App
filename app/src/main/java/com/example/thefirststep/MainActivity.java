package com.example.thefirststep;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
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
        requestStoragePermission();

        // Firebase setup
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("beneficiary");

        // UI references
        goToEntryButton = findViewById(R.id.goToEntryButton);
        recyclerView = findViewById(R.id.recyclerViewMain);
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);

        searchView.setIconified(true);
        searchView.requestFocus();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BeneficiaryAdapter(beneficiaryList, this);
        recyclerView.setAdapter(adapter);

        // Set up Toolbar and Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ImageButton downloadPdfButton = findViewById(R.id.downloadPdfButton);
        downloadPdfButton.setOnClickListener(v -> {
            generatePdfFromFirebase();
        });


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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

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

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        else {
            // Android 6 to 10
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }


    private void generatePdfFromFirebase() {
        FirebaseDatabase.getInstance().getReference("beneficiary")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String fileName = "Beneficiaries_" + System.currentTimeMillis() + ".pdf";
                            File file = new File(getExternalFilesDir(null), fileName);
                            FileOutputStream outputStream = new FileOutputStream(file);


                            // 2. Start writing PDF using iText
                            Document document = new Document();
                            PdfWriter.getInstance(document, outputStream);
                            document.open();

                            document.add(new Paragraph("List of Beneficiaries\n\n"));

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Beneficiary b = dataSnapshot.getValue(Beneficiary.class);
                                if (b != null) {
                                    StringBuilder entry = new StringBuilder();
                                    entry.append("Key: ").append(b.getKey()).append("\n");
                                    entry.append("Name: ").append(b.getName()).append("\n");
                                    entry.append("Contact: ").append(b.getContact()).append("\n");
                                    entry.append("Gender: ").append(b.getGender()).append("\n");
                                    entry.append("Age: ").append(b.getBirthDate()).append("\n");
                                    entry.append("Occupation: ").append(b.getOccupation()).append("\n");
                                    entry.append("Address: ").append(b.getAddress()).append("\n");
                                    entry.append("Status: ").append(b.getStatus()).append("\n");
                                    entry.append("Side: ").append(b.getSide()).append("\n");
                                    entry.append("Level: ").append(b.getLevel()).append("\n");
                                    entry.append("Year of Amputation: ").append(b.getYearOfAmputation()).append("\n");
                                    entry.append("History: ").append(b.getHistory()).append("\n");
                                    entry.append("Next of Kin: ").append(b.getNameKin()).append("\n");
                                    entry.append("Kin Contact: ").append(b.getContactKin()).append("\n");
                                    entry.append("--------------------------------------------\n\n");

                                    document.add(new com.itextpdf.text.Paragraph(entry.toString()));
                                }
                            }

                            document.close();
                            Toast.makeText(MainActivity.this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                            Uri uri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    getPackageName() + ".provider",
                                    file
                            );

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Failed to create PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadBeneficiaries() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Beneficiary> loadedList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Beneficiary beneficiary = dataSnapshot.getValue(Beneficiary.class);
                    if (beneficiary != null) {
                        loadedList.add(beneficiary);
                    }
                }

                beneficiaryList.clear();
                beneficiaryList.addAll(loadedList);

                // Update the adapter's full list if using search
                if (adapter != null) {
                    adapter.updateFullList(loadedList);
                    adapter.notifyDataSetChanged();
                }
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
