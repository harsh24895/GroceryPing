package com.example.groceryping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {
    private static final String TAG = "BarcodeScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private PreviewView viewFinder;
    private FloatingActionButton flashButton;
    private ExecutorService cameraExecutor;
    private boolean isFlashOn = false;
    private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_barcode_scanner);
            Log.d(TAG, "Layout inflated successfully");

            viewFinder = findViewById(R.id.viewFinder);
            flashButton = findViewById(R.id.flashButton);

            if (viewFinder == null) {
                Log.e(TAG, "viewFinder is null");
                Toast.makeText(this, "Error initializing camera view", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Request camera permissions
            if (allPermissionsGranted()) {
                Log.d(TAG, "Camera permissions granted, starting camera");
                startCamera();
            } else {
                Log.d(TAG, "Requesting camera permissions");
                ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }

            // Set up the flash button
            flashButton.setOnClickListener(v -> toggleFlash());

            cameraExecutor = Executors.newSingleThreadExecutor();
            Log.d(TAG, "Activity created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCamera() {
        try {
            Log.d(TAG, "Starting camera initialization");
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    Log.d(TAG, "Getting camera provider");
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error getting camera provider", e);
                    Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, ContextCompat.getMainExecutor(this));
        } catch (Exception e) {
            Log.e(TAG, "Error in startCamera", e);
            Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            Log.d(TAG, "Binding camera preview");
            // Unbind any previous use cases
            cameraProvider.unbindAll();

            // Set up the preview use case
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

            // Set up the camera selector
            CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

            // Set up the image analysis use case
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

            imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis);
            
            Log.d(TAG, "Camera preview bound successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera preview", e);
            Toast.makeText(this, "Error binding camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void analyzeImage(@NonNull ImageProxy image) {
        try {
            if (image.getImage() == null) {
                Log.e(TAG, "Image is null");
                image.close();
                return;
            }

            InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(), image.getImageInfo().getRotationDegrees());

            BarcodeScanning.getClient()
                .process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null) {
                            Log.d(TAG, "Barcode detected: " + rawValue);
                            handleBarcodeResult(rawValue);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Barcode scanning failed", e))
                .addOnCompleteListener(task -> image.close());
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing image", e);
            image.close();
        }
    }

    private void handleBarcodeResult(String barcodeValue) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "Handling barcode result: " + barcodeValue);
                getIntent().putExtra("barcode", barcodeValue);
                setResult(RESULT_OK, getIntent());
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Error handling barcode result", e);
                Toast.makeText(this, "Error processing barcode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFlash() {
        try {
            isFlashOn = !isFlashOn;
            flashButton.setImageResource(isFlashOn ? 
                R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            // TODO: Implement flash control
        } catch (Exception e) {
            Log.e(TAG, "Error toggling flash", e);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                Log.d(TAG, "Camera permission granted, starting camera");
                startCamera();
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, 
                    "Camera permission is required for barcode scanning", 
                    Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (cameraProvider != null) {
                Log.d(TAG, "Unbinding camera use cases");
                cameraProvider.unbindAll();
            }
            if (cameraExecutor != null) {
                Log.d(TAG, "Shutting down camera executor");
                cameraExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
} 