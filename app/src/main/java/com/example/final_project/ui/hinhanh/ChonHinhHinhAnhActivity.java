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
                        // Lấy khuôn mặt đầu tiên tìm thấy
                        Face face = faces.get(0);

                        // Lấy tọa độ khung chữ nhật bao quanh khuôn mặt
                        Rect bounds = face.getBoundingBox();

                        // Xử lý an toàn: Đảm bảo tọa độ cắt không vượt ra ngoài viền ảnh
                        int x = Math.max(0, bounds.left);
                        int y = Math.max(0, bounds.top);
                        int width = Math.min(bitmap.getWidth() - x, bounds.width());
                        int height = Math.min(bitmap.getHeight() - y, bounds.height());

                        // DÙNG KÉO CẮT ẢNH: Tạo một tấm ảnh nhỏ chỉ chứa khuôn mặt
                        Bitmap croppedFace = Bitmap.createBitmap(bitmap, x, y, width, height);

                        Log.d("MLKIT", "Đã cắt khuôn mặt thành công.");

                        // Đưa CÁI MẶT VỪA CẮT cho AI phân tích
                        runAIModel(croppedFace);

                    } else {
                        Log.d("MLKIT", "Không tìm thấy khuôn mặt.");
                        txtKetQua.setText("Không phải ảnh khuôn mặt");
                        txtKetQua.setTextColor(Color.RED);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MLKIT", "Lỗi khi phát hiện khuôn mặt", e);
                    txtKetQua.setText("Lỗi kiểm tra ảnh");
                    txtKetQua.setTextColor(Color.RED);
                });
    }

    // --- HÀM MỚI: THU NHỎ VÀ ÉP THÀNH TRẮNG ĐEN ---
    private Bitmap getLowResGrayscaleBitmap(Bitmap original, int width, int height) {
        // 1. Hạ độ phân giải
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(original, width, height, true);

        // 2. Ép sang trắng đen (Grayscale)
        Bitmap grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayscaleBitmap);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0); // Về 0 là trắng đen

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(resizedBitmap, 0, 0, paint);

        return grayscaleBitmap;
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

            // 1. Đưa khuôn mặt vừa cắt qua hàm làm mờ (48x48) và ép trắng đen
            Bitmap lowResGrayFace = getLowResGrayscaleBitmap(bitmap, 48, 48);

            // 2. Hiển thị cái ảnh trắng đen 48x48 lên màn hình cho dễ nhìn (Debug)
            runOnUiThread(() -> imgHienThi.setImageBitmap(lowResGrayFace));

            // 3. Đưa vào Buffer
            ByteBuffer inputBuffer = convertBitmapToGrayByteBuffer(lowResGrayFace);
            float[][] outputBuffer = new float[1][2];

            tflite.run(inputBuffer, outputBuffer);

            float probNormal = outputBuffer[0][0];
            float probDepression = outputBuffer[0][1];

            if (probDepression > probNormal) {
                txtKetQua.setTextColor(Color.RED);
                txtKetQua.setText("CÓ DẤU HIỆU TRẦM CẢM");
            } else {
                txtKetQua.setTextColor(Color.parseColor("#008000"));
                txtKetQua.setText("TÂM TRẠNG BÌNH THƯỜNG");
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtKetQua.setText("Lỗi AI: " + e.getMessage());
            txtKetQua.setTextColor(Color.RED);
        }
    }

    // --- ĐÃ ĐƠN GIẢN HÓA VÌ ẢNH LÚC NÀY CHẮC CHẮN ĐÃ LÀ TRẮNG ĐEN ---
    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 48 * 48 * 1);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[48 * 48];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixelValue : intValues) {
            int r = (pixelValue >> 16) & 0xFF;

            // THỬ NGHIỆM TRƯỜNG HỢP 1: Dải [-1, 1]
            float normalizedPixel = (r - 127.5f) / 127.5f;

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