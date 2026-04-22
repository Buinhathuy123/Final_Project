package com.example.final_project.ui.hinhanh;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ChonHinhHinhAnhActivity extends AppCompatActivity {

    private ImageView imgHienThi;
    private TextView txtKetQua;
    private LinearLayout layoutChonHinh;
    private View btnBack;
    private LinearLayout btnNutXanh;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private Interpreter tflite;
    private FaceDetector faceDetector;

    // ⭐ THÊM USERNAME
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chonhinh_hinhanh);

        initView();
        setupResultLaunchers();

        // ⭐ NHẬN USERNAME TỪ MÀN TRƯỚC
        username = getIntent().getStringExtra("username");

        // 1. Khởi tạo ML Kit Face Detector
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();
        faceDetector = FaceDetection.getClient(options);

        // 2. LOAD MODEL TFLite
        try {
            File modelFile = getFileFromAssets(this, "model_48x48.tflite");
            tflite = new Interpreter(modelFile);
        } catch (Exception e) {
            e.printStackTrace();
            txtKetQua.setText("Lỗi khởi tạo AI: " + e.getMessage());
        }

        if (btnNutXanh != null) {
            btnNutXanh.setOnClickListener(v -> showImageSourceDialog());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void initView() {
        layoutChonHinh = findViewById(R.id.rljd9mh7662b);
        imgHienThi = findViewById(R.id.ro29yxj795bn);
        txtKetQua = findViewById(R.id.r93crw50r0cd);
        btnBack = findViewById(R.id.rdi8fnk6ypo4);
        btnNutXanh = findViewById(R.id.rzhw6nwu4krg);
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh để kiểm tra");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkCameraPermission()) openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void setupResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) hienThiVaDuDoan(bitmap);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            hienThiVaDuDoan(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return false;
        }
        return true;
    }

    private void hienThiVaDuDoan(Bitmap bitmap) {
        if (bitmap == null) return;

        imgHienThi.setImageBitmap(bitmap);
        txtKetQua.setText("Đang tìm khuôn mặt...");
        txtKetQua.setTextColor(Color.BLACK);
        kiemTraKhuonMatVaDuDoan(bitmap);
    }

    private void kiemTraKhuonMatVaDuDoan(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces != null && !faces.isEmpty()) {

                        Face face = faces.get(0);
                        Rect bounds = face.getBoundingBox();

                        int x = Math.max(0, bounds.left);
                        int y = Math.max(0, bounds.top);
                        int width = Math.min(bitmap.getWidth() - x, bounds.width());
                        int height = Math.min(bitmap.getHeight() - y, bounds.height());

                        Bitmap croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height);

                        runAIModel(croppedFace);

                    } else {
                        txtKetQua.setText("Không phải ảnh khuôn mặt");
                        txtKetQua.setTextColor(Color.RED);
                    }
                });
    }

    private Bitmap getLowResGrayscaleBitmap(Bitmap original, int width, int height) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(original, width, height, true);
        Bitmap grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(grayscaleBitmap);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(resizedBitmap, 0, 0, paint);

        return grayscaleBitmap;
    }

    private void runAIModel(Bitmap bitmap) {
        if (tflite == null) return;

        try {
            txtKetQua.setText("Đang phân tích tâm trạng...");

            Bitmap input = getLowResGrayscaleBitmap(bitmap, 48, 48);
            ByteBuffer buffer = convertBitmapToGrayByteBuffer(input);

            float[][] output = new float[1][2];
            tflite.run(buffer, output);

            boolean isDepressed = output[0][1] > output[0][0];

            com.example.final_project.util.DataManager.saveFaceResult(this, isDepressed);

            Intent intent = new Intent(this, KetQuaHinhAnhActivity.class);
            intent.putExtra("isDepressed", isDepressed);

            // ⭐ THÊM DÒNG NÀY
            intent.putExtra("username", username);

            startActivity(intent);
            finish();

        } catch (Exception e) {
            txtKetQua.setText("Lỗi AI");
        }
    }

    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 48 * 48);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[48 * 48];
        bitmap.getPixels(pixels, 0, 48, 0, 0, 48, 48);

        for (int p : pixels) {
            float v = ((p >> 16) & 0xFF) / 255.0f;
            byteBuffer.putFloat(v);
        }

        return byteBuffer;
    }

    private File getFileFromAssets(Context context, String fileName) throws IOException {
        File file = new File(context.getCacheDir(), fileName);
        if (!file.exists()) {
            InputStream is = context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return file;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) tflite.close();
        if (faceDetector != null) faceDetector.close();
    }
}