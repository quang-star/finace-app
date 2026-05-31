package com.example.personalfinance.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.ActivityScanProductBinding;
import com.example.personalfinance.fragments.transaction.AddTransactionFragment;
import com.example.personalfinance.models.AiProductClassificationResult;
import com.example.personalfinance.models.AiProductLog;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.ProductClassifyRequest;
import com.example.personalfinance.models.SaveProductLogRequest;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanProductActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 1002;
    private static final int GALLERY_PICK_CODE = 1003;
    private ActivityScanProductBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private User currentUser;

    // Detect state
    private String detectedProductName = "";
    private double detectedConfidence = 0.0;

    // Mock products for fallback TFLite
    private final String[] mockProducts = {"Cà phê sữa", "Bánh mì kẹp thịt", "Bún chả", "Xăng xe máy", "Trà sữa trân châu", "Áo thun nam", "Sách giáo trình"};
    private final double[] mockConfidences = {0.94, 0.89, 0.96, 0.98, 0.87, 0.92, 0.90};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = SharedPrefManager.getInstance(this).getUser();
        if (currentUser == null) {
            finish();
            return;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCapture.setOnClickListener(v -> captureProduct());
        binding.btnGallery.setOnClickListener(v -> openGallery());
        binding.btnSaveProduct.setOnClickListener(v -> saveAndConfirmProduct());

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Lỗi khởi động camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi liên kết camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void captureProduct() {
        if (imageCapture == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);
        binding.cardProductInfo.setVisibility(View.GONE);

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                imageProxy.close();
                runMockDetection();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnCapture.setEnabled(true);
                Toast.makeText(ScanProductActivity.this, "Lỗi chụp ảnh sản phẩm: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runMockDetection() {
        // Simulates a YOLO TFLite object detection pipeline on the taken frame
        int index = (int) (Math.random() * mockProducts.length);
        detectedProductName = mockProducts[index];
        detectedConfidence = mockConfidences[index];

        binding.progressBar.setVisibility(View.GONE);
        binding.btnCapture.setEnabled(true);

        binding.tvProductLabel.setText("Sản phẩm: " + detectedProductName);
        binding.tvConfidence.setText("Độ tin cậy: " + (int)(detectedConfidence * 100) + "%");
        binding.etPrice.setText("");
        binding.cardProductInfo.setVisibility(View.VISIBLE);
        binding.etPrice.requestFocus();
    }

    private void saveAndConfirmProduct() {
        String priceText = binding.etPrice.getText().toString().trim();

        if (priceText.isEmpty()) {
            binding.etPrice.setError("Vui lòng nhập giá sản phẩm");
            binding.etPrice.requestFocus();
            return;
        }

        double enteredPrice;
        try {
            enteredPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            binding.etPrice.setError("Giá tiền không hợp lệ");
            binding.etPrice.requestFocus();
            return;
        }

        binding.btnSaveProduct.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // 1. Call Random Forest to categorize the detected product name
        ProductClassifyRequest classifyRequest = new ProductClassifyRequest(currentUser.getUserId(), detectedProductName);

        RetrofitClient.getApiService().classifyProduct(classifyRequest).enqueue(new Callback<ApiResponse<AiProductClassificationResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiProductClassificationResult>> call, Response<ApiResponse<AiProductClassificationResult>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AiProductClassificationResult result = response.body().getData();
                    if (result != null) {
                        logAndOpenTransaction(enteredPrice, result.getSuggestedCategoryId(), result.getSuggestedCategoryName());
                    } else {
                        Toast.makeText(ScanProductActivity.this, "Không phân loại được sản phẩm", Toast.LENGTH_SHORT).show();
                        binding.btnSaveProduct.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Lỗi phân loại chi tiêu";
                    Toast.makeText(ScanProductActivity.this, msg, Toast.LENGTH_SHORT).show();
                    binding.btnSaveProduct.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiProductClassificationResult>> call, Throwable t) {
                Toast.makeText(ScanProductActivity.this, "Lỗi kết nối phân loại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnSaveProduct.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void logAndOpenTransaction(double price, Integer categoryId, String categoryName) {
        // 2. Submit log to AI product logs
        SaveProductLogRequest logRequest = new SaveProductLogRequest(
                currentUser.getUserId(),
                detectedProductName,
                detectedConfidence,
                price,
                categoryId
        );

        RetrofitClient.getApiService().saveProductLog(logRequest).enqueue(new Callback<ApiResponse<AiProductLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiProductLog>> call, Response<ApiResponse<AiProductLog>> response) {
                binding.btnSaveProduct.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Open prefilled transaction form
                    AddTransactionFragment addFragment = AddTransactionFragment.newInstance(
                            price,
                            detectedProductName,
                            categoryId != null ? categoryId : 0,
                            DateUtils.getCurrentDateString(),
                            0 // No OCR feedback needed for YOLO product scans
                    );
                    addFragment.show(getSupportFragmentManager(), "AddTransactionFragment");
                } else {
                    Toast.makeText(ScanProductActivity.this, "Lỗi lưu log sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiProductLog>> call, Throwable t) {
                binding.btnSaveProduct.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ScanProductActivity.this, "Lỗi kết nối lưu log: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Simply runs mock detection on the selected gallery image
            runMockDetection();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Quyền truy cập Camera bị từ chối. Không thể quét sản phẩm!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
