package com.example.final_project.ui.hinhanh;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
// IMPORT ĐÚNG: Trỏ đến Activity BẮT ĐẦU của phần Trắc nghiệm
// Bạn hãy kiểm tra lại package thực tế của BatDauTracNghiemActivity trong project nhé
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.util.DataManager;

public class KetQuaHinhAnhActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_hinhanh);

        // 1. Ánh xạ View
        TextView txtKetQua = findViewById(R.id.rxkmwbhgwgz);
        TextView txtMoTa = findViewById(R.id.rrk64zpie7uj);
        TextView txtGoiY = findViewById(R.id.txtGoiY);
        LinearLayout btnKetThuc = findViewById(R.id.raf9m3etc9f6);
        View btnGoToQuiz = findViewById(R.id.btnGoToQuiz); // Nút điều hướng trong XML

        // 2. Nhận dữ liệu kết quả hình ảnh
        boolean isDepressed = getIntent().getBooleanExtra("isDepressed", false);

        if (isDepressed) {
            txtKetQua.setText("CÓ DẤU HIỆU TRẦM CẢM");
            txtKetQua.setTextColor(Color.parseColor("#FF0000"));
            txtMoTa.setText("Phân tích khuôn mặt cho thấy các biểu hiện của sự mệt mỏi.");
        } else {
            txtKetQua.setText("TÂM TRẠNG BÌNH THƯỜNG");
            txtKetQua.setTextColor(Color.parseColor("#008000"));
            txtMoTa.setText("Tình trạng tinh thần của bạn hiện tại khá ổn định thông qua biểu cảm khuôn mặt.");
        }

        // 3. Lưu kết quả vào hệ thống
        DataManager.saveFaceResult(this, isDepressed);

        // 4. Kiểm tra xem bài Trắc nghiệm đã hoàn thành chưa để hiện gợi ý
        if (!DataManager.isQuizCompleted(this)) {
            txtGoiY.setText("✨ Để có kết quả chính xác hơn, bạn hãy thử làm thêm bài 'Trắc nghiệm tâm lý' nhé!");
            txtGoiY.setVisibility(View.VISIBLE);
            if (btnGoToQuiz != null) btnGoToQuiz.setVisibility(View.VISIBLE);
        } else {
            txtGoiY.setVisibility(View.GONE);
            if (btnGoToQuiz != null) btnGoToQuiz.setVisibility(View.GONE);
        }

        // 5. SỬA ĐIỀU HƯỚNG: Chuyển sang BatDauTracNghiemActivity
        if (btnGoToQuiz != null) {
            btnGoToQuiz.setOnClickListener(v -> {
                // Điều hướng người dùng đến trang bắt đầu của bài test Quiz
                Intent intent = new Intent(KetQuaHinhAnhActivity.this, BatDauTracNghiemActivity.class);
                startActivity(intent);
            });
        }

        // 6. Nút Kết thúc về trang chủ
        btnKetThuc.setOnClickListener(v -> {
            Intent intent = new Intent(KetQuaHinhAnhActivity.this, TrangChuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}