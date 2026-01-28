package com.example.final_project.ui.hinhanh;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
// --- LƯU Ý: Đảm bảo bạn đã import file model_48x48.tflite vào thư mục ml ---
import com.example.final_project.ml.Model48x48;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;

import java.io.IOException;

public class ChonHinhHinhAnhActivity extends AppCompatActivity {

    private ImageView imgHienThi;
    private TextView txtKetQua;
    private LinearLayout layoutChonHinh;
    private View btnBack;
    private LinearLayout btnNutXanh;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chonhinh_hinhanh);

        initView();
        setupResultLaunchers();

        if (btnNutXanh != null) {
            btnNutXanh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImageSourceDialog();
                }
            });
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
                        try {
                            // Lấy ảnh thumbnail từ camera
                            Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                            if (bitmap != null) {
                                hienThiVaDuDoan(bitmap);
                            } else {
                                Toast.makeText(this, "Không lấy được ảnh!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi lấy ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return false;
        }
        return true;
    }

    private void hienThiVaDuDoan(Bitmap bitmap) {
        if (bitmap == null) return;

        // Hiển thị ảnh
        imgHienThi.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        imgHienThi.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
        imgHienThi.requestLayout();
        imgHienThi.setImageBitmap(bitmap);
        imgHienThi.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Chạy Model
        runAIModel(bitmap);
    }

    private void runAIModel(Bitmap bitmap) {
        try {
            txtKetQua.setText("Đang phân tích...");

            // 1. Khởi tạo Model
            Model48x48 model = Model48x48.newInstance(getApplicationContext());

            // 2. Resize ảnh
            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(48, 48, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new TransformToGrayscaleOp())
                    .build();

            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);

            // 3. Chạy dự đoán (QUAN TRỌNG: Có .getTensorBuffer())
            Model48x48.Outputs outputs = model.process(tensorImage.getTensorBuffer());

            float[] confidences = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

            // 4. Lấy kết quả
            String[] classes = {"Bình thường", "Có dấu hiệu trầm cảm"};
            int maxPos = 0;
            float maxScore = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxScore) {
                    maxScore = confidences[i];
                    maxPos = i;
                }
            }

            String resultText = "Kết quả: " + classes[maxPos] + "\nĐộ tin cậy: " + String.format("%.1f%%", maxScore * 100);
            txtKetQua.setText(resultText);

            model.close();

        } catch (Exception e) {
            // Nếu lỗi, nó sẽ hiện lên màn hình thay vì làm sập app
            e.printStackTrace();
            txtKetQua.setText("Lỗi AI: " + e.getMessage());
        }
    }
}