package com.example.thefirststep;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;

public class EntryActivity extends AppCompatActivity {

    private EditText editTextKey, editTextName, editTextBirthDate, editTextContact, editTextHistory;
    private EditText editTextAddress, editTextYearOfAmputation, editTextContactKin, editTextNameKin, editTextOccupation;
    private Button saveButton, updateButton, backButton;
    private Spinner SpinnerStatus, levelOfAmputationSpinner, sideOfAmputationSpinner, textGenderSpinner;
    private ArrayAdapter<CharSequence> adapterStatus, adapterLevel, adapterSide, adapterGender;
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
        editTextHistory = findViewById(R.id.editTextHistory);
        textGenderSpinner = findViewById(R.id.spinnerTextGender);
        editTextBirthDate  = findViewById(R.id.editTextBirthDate);
        editTextOccupation = findViewById(R.id.editTextOccupation);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextYearOfAmputation = findViewById(R.id.editTextYearOfAmputation);
        editTextContactKin = findViewById(R.id.editTextContactKin);
        editTextNameKin = findViewById(R.id.editTextNameKin);

        SpinnerStatus = findViewById(R.id.SpinnerStatus);
        levelOfAmputationSpinner = findViewById(R.id.levelOfAmputation);
        sideOfAmputationSpinner = findViewById(R.id.sideOfAmputation);

        saveButton = findViewById(R.id.saveButton);
        updateButton = findViewById(R.id.updateButton);
        backButton = findViewById(R.id.backButton);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        beneficiaryImageView = findViewById(R.id.uploadedImageView);

        storage = FirebaseStorage.getInstance();

        adapterStatus = ArrayAdapter.createFromResource(this, R.array.SpinnerStatus, android.R.layout.simple_spinner_item);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpinnerStatus.setAdapter(adapterStatus);

        adapterGender = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textGenderSpinner.setAdapter(adapterGender);

        adapterLevel = ArrayAdapter.createFromResource(this, R.array.levelOfAmputation, android.R.layout.simple_spinner_item);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelOfAmputationSpinner.setAdapter(adapterLevel);

        adapterSide = ArrayAdapter.createFromResource(this, R.array.sideOfAmputation, android.R.layout.simple_spinner_item);
        adapterSide.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sideOfAmputationSpinner.setAdapter(adapterSide);

        progressBar = findViewById(R.id.progressBar);

        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 101);
        });

        if (getIntent().hasExtra("KEY")) {
            populateFieldsFromIntent();
        } else {
            generateNextKey();
            saveButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.GONE);
        }

        setupBirthDatePicker();

        saveButton.setOnClickListener(v -> saveNewBeneficiary());
        updateButton.setOnClickListener(v -> updateBeneficiary());
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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
        editTextKey.setText(getIntent().getStringExtra("KEY"));
        editTextName.setText(getIntent().getStringExtra("NAME"));
        editTextContact.setText(getIntent().getStringExtra("CONTACT"));
        editTextBirthDate.setText(getIntent().getStringExtra("BIRTH_DATE"));
        editTextOccupation.setText(getIntent().getStringExtra("OCCUPATION"));
        editTextAddress.setText(getIntent().getStringExtra("ADDRESS"));
        editTextNameKin.setText(getIntent().getStringExtra("KIN_NAME"));
        editTextContactKin.setText(getIntent().getStringExtra("KIN_CONTACT"));
        editTextYearOfAmputation.setText(getIntent().getStringExtra("YEAR_OF_AMPUTATION"));
        editTextHistory.setText(getIntent().getStringExtra("HISTORY"));

        photoUrl = getIntent().getStringExtra("PHOTO_URL");

        String status = getIntent().getStringExtra("STATUS");
        if (status != null) {
            int spinnerPosition = adapterStatus.getPosition(status);
            SpinnerStatus.setSelection(spinnerPosition);
        }

        String level = getIntent().getStringExtra("LEVEL");
        if (level != null) {
            int spinnerPosition = adapterLevel.getPosition(level);
            levelOfAmputationSpinner.setSelection(spinnerPosition);
        }

        String gender = getIntent().getStringExtra("GENDER");
        if (gender != null) {
            int spinnerPosition = adapterGender.getPosition(gender);
            textGenderSpinner.setSelection(spinnerPosition);
        }

        String side = getIntent().getStringExtra("SIDE");
        if (side != null) {
            int spinnerPosition = adapterSide.getPosition(side);
            sideOfAmputationSpinner.setSelection(spinnerPosition);
        }

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.person_24)
                    .into(beneficiaryImageView);
        }

        editTextKey.setEnabled(false);
        saveButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.VISIBLE);
    }

    private void generateNextKey() {
        databaseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long max = 0;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String key = snapshot.getKey();
                    if (key != null && key.startsWith("A")) {
                        try {
                            long number = Long.parseLong(key.substring(1));
                            if (number > max) {
                                max = number;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                String newKey = String.format("A%03d", max + 1);
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
        String status = SpinnerStatus.getSelectedItem().toString();
        String history = editTextHistory.getText().toString().trim();
        String level = levelOfAmputationSpinner.getSelectedItem().toString();
        String side = sideOfAmputationSpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            uploadImageToCloudinary(selectedImageUri, (photoUrl) -> {
                progressBar.setVisibility(View.GONE);
                saveBeneficiaryToDatabase(key, name, contact, level, side, status, history, photoUrl);
            });
        } else {
            saveBeneficiaryToDatabase(key, name, contact, level, side, status, history, null);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    public void uploadImageToCloudinary(Uri imageUri, CloudinaryUploadCallback callback) {
        try {
            InputStream iStream = getContentResolver().openInputStream(imageUri);
            byte[] inputData = getBytes(iStream);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "upload.jpg",
                            RequestBody.create(inputData, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", "The_FirstStep") // unsigned preset name
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/dqxhu2dnl/image/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(EntryActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(EntryActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_LONG).show());
                        return;
                    }

                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        String imageUrl = jsonObject.getString("secure_url");
                        runOnUiThread(() -> callback.onUploadSuccess(imageUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    interface CloudinaryUploadCallback {
        void onUploadSuccess(String imageUrl);
    }

    private void saveBeneficiaryToDatabase(String key, String name, String contact, String level, String side,
                                           String status, String history, String photoUrl) {
        String gender = textGenderSpinner.getSelectedItem().toString();
        String birthDate = editTextBirthDate.getText().toString().trim();
        String occupation = editTextOccupation.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String yearOfAmputation = editTextYearOfAmputation.getText().toString().trim();
        String nameKin = editTextNameKin.getText().toString().trim();
        String contactKin = editTextContactKin.getText().toString().trim();

        Beneficiary beneficiary = new Beneficiary(
                key, name, contact, level, side, status, history, photoUrl,
                gender, birthDate, occupation, address, yearOfAmputation, nameKin, contactKin
        );

        databaseRef.child(key).setValue(beneficiary)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EntryActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
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
        String level = levelOfAmputationSpinner.getSelectedItem().toString();
        String status = SpinnerStatus.getSelectedItem().toString();
        String history = editTextHistory.getText().toString().trim();
        String birthDate = editTextBirthDate.getText().toString().trim();
        String gender = textGenderSpinner.getSelectedItem().toString();
        String occupation = editTextOccupation.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String yearOfAmputation = editTextYearOfAmputation.getText().toString().trim();
        String nameKin = editTextNameKin.getText().toString().trim();
        String contactKin = editTextContactKin.getText().toString().trim();
        String side = sideOfAmputationSpinner.getSelectedItem().toString();


        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        Beneficiary updated = new Beneficiary(
                key, name, contact, level, side, status, history, photoUrl,
                gender, birthDate, occupation, address, yearOfAmputation, nameKin, contactKin
        );

        databaseRef.child(key).setValue(updated).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EntryActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EntryActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBirthDatePicker() {
        editTextBirthDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    EntryActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        editTextBirthDate.setText(selectedDate);
                    },
                    year, month, day
            );

            // Optional: If you want a white background dialog
            datePicker.getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);

            // Optional: Set a max date to prevent future dates
            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePicker.show();
        });
    }
}