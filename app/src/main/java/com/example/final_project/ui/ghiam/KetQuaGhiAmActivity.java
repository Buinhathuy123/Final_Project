package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
// SỬA LẠI IMPORT: Trỏ đến trang BẮT ĐẦU làm bài test hình ảnh
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.util.DataManager;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtMoTa, txtKetQuaTongHop, txtGoiY;
    private View btnGoToFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        // 1. Ánh xạ View
        txtMoTa = findViewById(R.id.txtMoTa);
        txtKetQuaTongHop = findViewById(R.id.txtKetQuaCuoiCung);
        txtGoiY = findViewById(R.id.txtGoiY);
        btnGoToFace = findViewById(R.id.btnGoToFace);

        // 2. Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        int labelAudio = intent.getIntExtra("label_voice", 0);

        // 3. Hiển thị kết quả và gợi ý
        displayVoiceResult(labelAudio);
        checkAndShowSuggestion();

        // 4. Xử lý nút Hoàn tất (Về trang chủ)
        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            Intent i = new Intent(KetQuaGhiAmActivity.this, TrangChuActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // 5. SỬA ĐIỀU HƯỚNG: Chuyển sang trang BatDauHinhAnhActivity
        btnGoToFace.setOnClickListener(v -> {
            Intent i = new Intent(KetQuaGhiAmActivity.this, BatDauHinhAnhActivity.class);
            startActivity(i);
        });
    }

    private void displayVoiceResult(int labelAudio) {
        String resultTitle;
        String advice;
        int color;

        if (labelAudio == 1) {
            resultTitle = "CÓ DẤU HIỆU TRẦM CẢM";
            color = android.R.color.holo_red_dark;
            advice = "Phân tích giọng nói cho thấy các chỉ số liên quan đến trầm cảm.";
        } else {
            resultTitle = "TÂM TRẠNG BÌNH THƯỜNG";
            color = android.R.color.holo_green_dark;
            advice = "Giọng nói của bạn hiện tại không cho thấy dấu hiệu trầm cảm.";
        }

        txtKetQuaTongHop.setText(resultTitle);
        txtKetQuaTongHop.setTextColor(ContextCompat.getColor(this, color));
        txtMoTa.setText(advice);

        DataManager.saveVoiceResult(this, labelAudio);
    }

    private void checkAndShowSuggestion() {
        if (!DataManager.isFaceCompleted(this)) {
            txtGoiY.setText("✨ Phân tích giọng nói xong rồi! Bạn hãy thử làm thêm bài 'Phân tích Hình ảnh' để có đánh giá đầy đủ nhất nhé.");
            txtGoiY.setVisibility(View.VISIBLE);
            btnGoToFace.setVisibility(View.VISIBLE);
        } else {
            txtGoiY.setVisibility(View.GONE);
            btnGoToFace.setVisibility(View.GONE);
        }
    }
}