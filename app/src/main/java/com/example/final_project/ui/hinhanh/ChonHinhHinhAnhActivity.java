package com.example.final_project.ui.hinhanh;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect; // Import thêm cái này để dùng Bounding Box
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;

// --- CÁC IMPORT CHO GOOGLE ML KIT ---
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chonhinh_hinhanh);

        initView();
        setupResultLaunchers();

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
            Interpreter.Options interpreterOptions = new Interpreter.Options();
            tflite = new Interpreter(modelFile, interpreterOptions);
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

        // Hiện ảnh gốc lên màn hình để người dùng xem trước
        imgHienThi.setImageBitmap(bitmap);

        txtKetQua.setText("Đang tìm khuôn mặt...");
        txtKetQua.setTextColor(Color.BLACK);
        kiemTraKhuonMatVaDuDoan(bitmap);
    }

    // --- CẬP NHẬT TÍNH NĂNG TỰ ĐỘNG CẮT KHUÔN MẶT ---
    private void kiemTraKhuonMatVaDuDoan(Bitmap bitmap) {
        if (faceDetector == null) {
            runAIModel(bitmap);
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces != null && !faces.isEmpty()) {
                        // 1. Lấy khuôn mặt đầu tiên tìm thấy
                        Face face = faces.get(0);

                        // 2. Lấy tọa độ khung chữ nhật bao quanh khuôn mặt
                        Rect bounds = face.getBoundingBox();

                        // 3. Xử lý an toàn: Đảm bảo tọa độ cắt không vượt ra ngoài viền ảnh
                        int x = Math.max(0, bounds.left);
                        int y = Math.max(0, bounds.top);
                        int width = Math.min(bitmap.getWidth() - x, bounds.width());
                        int height = Math.min(bitmap.getHeight() - y, bounds.height());

                        // 4. DÙNG KÉO CẮT ẢNH: Tạo một tấm ảnh nhỏ chỉ chứa khuôn mặt
                        Bitmap croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height);

                        Log.d("MLKIT", "Đã cắt khuôn mặt thành công.");

                        // (Tùy chọn) Hiển thị ảnh khuôn mặt ĐÃ CẮT lên màn hình cho xịn sò!
                        imgHienThi.setImageBitmap(croppedFace);

                        // 5. Đưa CÁI MẶT VỪA CẮT cho AI phân tích (Thay vì ảnh gốc)
                        runAIModel(croppedFace);

                    } else {
                        // ❌ KHÔNG PHẢI KHUÔN MẶT
                        Log.d("MLKIT", "Không tìm thấy khuôn mặt.");
                        txtKetQua.setText("không phải ảnh khuôn mặt");
                        txtKetQua.setTextColor(Color.RED);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MLKIT", "Lỗi khi phát hiện khuôn mặt", e);
                    txtKetQua.setText("Lỗi kiểm tra ảnh");
                    txtKetQua.setTextColor(Color.RED);
                });
    }

    private void runAIModel(Bitmap bitmap) {
        if (tflite == null) {
            txtKetQua.setText("Lỗi: Chưa load được AI.");
            txtKetQua.setTextColor(Color.RED);
            return;
        }

        try {
            txtKetQua.setText("Đang phân tích tâm trạng...");
            txtKetQua.setTextColor(Color.BLACK);

            // Thu nhỏ CÁI MẶT ĐÃ CẮT về đúng chuẩn 48x48
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, true);
            ByteBuffer inputBuffer = convertBitmapToGrayByteBuffer(resizedBitmap);
            float[][] outputBuffer = new float[1][2];

            tflite.run(inputBuffer, outputBuffer);

            float probNormal = outputBuffer[0][0];
            float probDepression = outputBuffer[0][1];

            if (probDepression > probNormal) {
                txtKetQua.setTextColor(Color.RED);
                txtKetQua.setText("CÓ DẤU HIỆU TRẦM CẢM\n(Độ tin cậy: " + String.format("%.1f%%", probDepression * 100) + ")");
            } else {
                txtKetQua.setTextColor(Color.parseColor("#008000"));
                txtKetQua.setText("TÂM TRẠNG BÌNH THƯỜNG\n(Độ tin cậy: " + String.format("%.1f%%", probNormal * 100) + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtKetQua.setText("Lỗi AI: " + e.getMessage());
            txtKetQua.setTextColor(Color.RED);
        }
    }

    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 48 * 48 * 1);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[48 * 48];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue : intValues) {
            int r = (pixelValue >> 16) & 0xFF;
            int g = (pixelValue >> 8) & 0xFF;
            int b = pixelValue & 0xFF;
            float normalizedPixel = (r + g + b) / 3.0f / 255.0f;
            byteBuffer.putFloat(normalizedPixel);
        }
        return byteBuffer;
    }

    private File getFileFromAssets(Context context, String fileName) throws IOException {
        File file = new File(context.getCacheDir(), fileName);
        if (!file.exists() || file.length() == 0) {
            try (InputStream is = context.getAssets().open(fileName);
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
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