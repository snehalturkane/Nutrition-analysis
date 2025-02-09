package com.example.Nutrition_Analysis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.test_app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_INTENT_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static final String AZURE_OPENAI_API_URL = "API_KEY";
    private static final String GOOGLE_VISION_API_URL = "API_KEY";

    private Button captureImageButton, viewRecordsButton;
    private ImageView capturedImageView;
    private TextView visionApiResponse;
    private RequestQueue requestQueue;
    private DatabaseHelper databaseHelper;
    private String lastCapturedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageButton = findViewById(R.id.captureImageButton);
        viewRecordsButton = findViewById(R.id.viewRecordsButton);
        capturedImageView = findViewById(R.id.capturedImageView);
        visionApiResponse = findViewById(R.id.visionApiResponse);

        requestQueue = Volley.newRequestQueue(this);
        databaseHelper = new DatabaseHelper(this);

        // Initially hide the ImageView and API response
        capturedImageView.setVisibility(View.GONE);
        visionApiResponse.setVisibility(View.GONE);

        captureImageButton.setOnClickListener(v -> checkCameraPermission());
        viewRecordsButton.setOnClickListener(v -> viewSavedRecords());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // If permission already granted, open camera
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_INTENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_INTENT_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Show the captured image and API response section
            capturedImageView.setVisibility(View.VISIBLE);
            visionApiResponse.setVisibility(View.VISIBLE);
            captureImageButton.setText("Take another Image");

            capturedImageView.setImageBitmap(photo);

            // Save the image locally and get its file path
            lastCapturedImagePath = saveImageToInternalStorage(photo);

            // Send the image to Google Vision API for object localization
            sendImageToGoogleVision(photo);
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = getFilesDir();  // Internal storage directory
        String fileName = "captured_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void sendImageToGoogleVision(Bitmap bitmap) {
        String base64Image = encodeImageToBase64(bitmap);

        try {
            // Prepare the request payload for Google Vision API
            JSONObject jsonRequest = new JSONObject();
            JSONArray requestsArray = new JSONArray();
            JSONObject requestObject = new JSONObject();
            JSONObject imageObject = new JSONObject();
            JSONObject featureObject = new JSONObject();

            imageObject.put("content", base64Image);
            featureObject.put("type", "OBJECT_LOCALIZATION");
            featureObject.put("maxResults", 5);

            requestObject.put("image", imageObject);
            requestObject.put("features", new JSONArray().put(featureObject));
            requestsArray.put(requestObject);
            jsonRequest.put("requests", requestsArray);

            // Send the POST request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    GOOGLE_VISION_API_URL,
                    jsonRequest,
                    this::handleVisionResponse,
                    error -> Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleVisionResponse(JSONObject response) {
        try {
            JSONArray localizedObjectAnnotations = response.getJSONArray("responses")
                    .getJSONObject(0)
                    .optJSONArray("localizedObjectAnnotations");

            if (localizedObjectAnnotations == null || localizedObjectAnnotations.length() == 0) {
                visionApiResponse.setText("Unable to identify the object. Please retake the image.");
                return;
            }

            String detectedObject = localizedObjectAnnotations.getJSONObject(0).getString("name");

            // Reject generic names and ask for retake
            if (detectedObject.equalsIgnoreCase("fruit") || detectedObject.equalsIgnoreCase("food") || detectedObject.equalsIgnoreCase("plant")) {
                visionApiResponse.setText("Unable to identify the exact item. Please retake the image.");
            } else {
                visionApiResponse.setText("Detected object: " + detectedObject);
                getNutritionDetailsFromAzureOpenAI(detectedObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            visionApiResponse.setText("Error processing image. Please retake it.");
        }
    }

    private void getNutritionDetailsFromAzureOpenAI(String detectedObject) {
        String prompt = "Provide only the nutritional facts (calories, protein, fat, carbohydrates, fiber, sugars) of " + detectedObject +
                ". If it has no nutrition, say: '" + detectedObject + " does not contain any nutritional value.' No additional details.";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", "You are a nutrition assistant that provides precise answers."))
                    .put(new JSONObject().put("role", "user").put("content", prompt))
            );
            requestBody.put("max_tokens", 150);
            requestBody.put("temperature", 0.3);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                AZURE_OPENAI_API_URL,
                requestBody,
                response -> {
                    try {
                        String completion = response.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        visionApiResponse.setText(completion);

                        // Save records if it contains nutrition details
                        if (!completion.toLowerCase().contains("does not contain any nutritional value")) {
                            saveNutritionDetails(lastCapturedImagePath, completion);
                        } else {
                            Toast.makeText(this, "No nutrition details to save.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        visionApiResponse.setText("Error parsing OpenAI response.");
                    }
                },
                error -> {
                    error.printStackTrace();
                    visionApiResponse.setText("Failed to fetch nutrition details.");
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void saveNutritionDetails(String imagePath, String nutritionDetails) {
        if (imagePath != null && nutritionDetails != null) {
            boolean success = databaseHelper.insertRecord(imagePath, nutritionDetails);
            if (success) {
                Toast.makeText(this, "Record saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid data. Unable to save.", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewSavedRecords() {
        Intent intent = new Intent(MainActivity.this, ViewRecordsActivity.class);
        startActivity(intent);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}
