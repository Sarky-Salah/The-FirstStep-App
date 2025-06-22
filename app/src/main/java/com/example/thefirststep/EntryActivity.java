package com.example.thefirststep;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EntryActivity extends AppCompatActivity {

    private EditText editTextKey, editTextName, editTextContact, editTextProstheticType, editTextHistory;
    private Button saveButton, updateButton, backButton;
    private Spinner SpinnerStatus;
    private ArrayAdapter<CharSequence> adapter;
    private ImageView beneficiaryImageView;
    private Button selectImageBtn;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private String photoUrl = null;
    private DatabaseReference databaseRef;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        databaseRef = FirebaseDatabase.getInstance().getReference("beneficiary");

        editTextKey = findViewById(R.id.editTextKey);
        editTextName = findViewById(R.id.editTextName);
        editTextContact = findViewById(R.id.editTextContact);
        editTextProstheticType = findViewById(R.id.editTextProstheticType);
        editTextHistory = findViewById(R.id.editTextHistory);
        SpinnerStatus = findViewById(R.id.SpinnerStatus);
        saveButton = findViewById(R.id.saveButton);
        updateButton = findViewById(R.id.updateButton);
        backButton = findViewById(R.id.backButton);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        storage = FirebaseStorage.getInstance();
        beneficiaryImageView = findViewById(R.id.uploadedImageView);

        // Set up spinner adapter
        adapter = ArrayAdapter.createFromResource(this, R.array.SpinnerStatus, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerStatus.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBar);

        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        // Check if editing existing beneficiary (intent contains "KEY")
        if (getIntent().hasExtra("KEY")) {
            populateFieldsFromIntent();
        } else {
            // New entry: generate a new unique key
            generateNextKey();
            saveButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.GONE);
        }

        // Button listeners
        saveButton.setOnClickListener(v -> saveNewBeneficiary());
        updateButton.setOnClickListener(v -> updateBeneficiary());
        backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            beneficiaryImageView.setImageURI(selectedImageUri);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
            Log.d("ImageUri", "Selected URI: " + selectedImageUri.toString());
        }
    }


    private void populateFieldsFromIntent() {
        String key = getIntent().getStringExtra("KEY");
        String name = getIntent().getStringExtra("NAME");
        String contact = getIntent().getStringExtra("CONTACT");
        String prostheticType = getIntent().getStringExtra("TYPE");
        String history = getIntent().getStringExtra("HISTORY");
        String status = getIntent().getStringExtra("STATUS");
        String image = getIntent().getStringExtra("PHOTO_URL");

        editTextKey.setText(key);
        editTextName.setText(name);
        editTextContact.setText(contact);
        editTextProstheticType.setText(prostheticType);
        editTextHistory.setText(history);

        this.photoUrl = image;

        if (status != null) {
            int spinnerPosition = adapter.getPosition(status);
            SpinnerStatus.setSelection(spinnerPosition);
        }

        editTextKey.setEnabled(false); // Key should not be editable for existing entries
        saveButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.VISIBLE);
    }

    private void generateNextKey() {
        databaseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long max = 0;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String key = snapshot.getKey(); // Example: "A001"
                    if (key != null && key.startsWith("A")) {
                        try {
                            long number = Long.parseLong(key.substring(1)); // Remove prefix 'A'
                            if (number > max) {
                                max = number;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                String newKey = String.format("A%03d", max + 1); // e.g. "A001", "A002"
                editTextKey.setText(newKey);
                editTextKey.setEnabled(false);
            } else {
                Toast.makeText(this, "Failed to generate key", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNewBeneficiary() {
        String key = editTextKey.getText().toString();
        String name = editTextName.getText().toString().trim();
        String contact = editTextContact.getText().toString().trim();
        String prostheticType = editTextProstheticType.getText().toString().trim();
        String status = SpinnerStatus.getSelectedItem().toString();
        String history = editTextHistory.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            uploadImageToCloudinary(selectedImageUri, (photoUrl) -> {
                progressBar.setVisibility(View.GONE);
                saveBeneficiaryToDatabase(key, name, contact, prostheticType, status, history, photoUrl);
            });
        } else {
            // No image, just save beneficiary without photoUrl
            saveBeneficiaryToDatabase(key, name, contact, prostheticType, status, history, null);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = { android.provider.MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }


    public void uploadImageToCloudinary(Uri imageUri, CloudinaryUploadCallback callback) {
        String filePath = getRealPathFromUri(imageUri); // You must implement this

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("image/*")))
                .addFormDataPart("upload_preset", "The FirstStep")  // unsigned preset name
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/dqxhu2dnl/image/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(EntryActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(EntryActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String jsonData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String imageUrl = jsonObject.getString("secure_url");
                    runOnUiThread(() -> {
                        callback.onUploadSuccess(imageUrl);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    interface CloudinaryUploadCallback {
        void onUploadSuccess(String imageUrl);
    }

    private void saveBeneficiaryToDatabase(String key, String name, String contact, String prostheticType, String status, String history, String photoUrl) {
        Beneficiary beneficiary = new Beneficiary(key, name, contact, prostheticType, status, history, photoUrl);
        databaseRef.child(key).setValue(beneficiary).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EntryActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();

                // Pass the photoUrl back to MainActivity via intent if you want:
                Intent intent = new Intent();
                intent.putExtra("PHOTO_URL", photoUrl);
                setResult(RESULT_OK, intent);

                finish();
            } else {
                Toast.makeText(EntryActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateBeneficiary() {
        String key = editTextKey.getText().toString();
        String name = editTextName.getText().toString().trim();
        String contact = editTextContact.getText().toString().trim();
        String prostheticType = editTextProstheticType.getText().toString().trim();
        String status = SpinnerStatus.getSelectedItem().toString();
        String history = editTextHistory.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        Beneficiary updated = new Beneficiary(key, name, contact, prostheticType, status, history, photoUrl);

        databaseRef.child(key).setValue(updated).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EntryActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EntryActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}