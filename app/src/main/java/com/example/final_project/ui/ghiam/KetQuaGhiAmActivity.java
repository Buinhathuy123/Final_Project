package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.util.DataManager;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtKetQuaTongHop, txtGoiY;
    private View btnGoToFace, cardGoiY; // Thêm cardGoiY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        // 1. Ánh xạ các View
        txtKetQuaTongHop = findViewById(R.id.txtKetQuaCuoiCung);
        txtGoiY = findViewById(R.id.txtGoiY);
        btnGoToFace = findViewById(R.id.btnGoToFace);
        cardGoiY = findViewById(R.id.cardGoiY); // Ánh xạ card bao quanh

        Intent intent = getIntent();
        int labelAudio = intent.getIntExtra("label_voice", 0);

        // 2. Hiển thị trạng thái với màu sắc chuyên nghiệp
        if (labelAudio == 1) {
            txtKetQuaTongHop.setText("CÓ DẤU HIỆU TRẦM CẢM");
            txtKetQuaTongHop.setTextColor(Color.parseColor("#E11D48")); // Màu đỏ Rose
        } else {
            txtKetQuaTongHop.setText("TÂM TRẠNG BÌNH THƯỜNG");
            txtKetQuaTongHop.setTextColor(Color.parseColor("#10B981")); // Màu xanh Emerald
        }

        // Lưu kết quả vào hệ thống[cite: 1]
        DataManager.saveVoiceResult(this, labelAudio);

        // 3. Kiểm tra bài test tiếp theo[cite: 1]
        checkAndShowSuggestion();

        // 4. Nút về trang chủ
        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            Intent i = new Intent(this, TrangChuActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // 5. Nút chuyển sang phân tích khuôn mặt
        if (btnGoToFace != null) {
            btnGoToFace.setOnClickListener(v -> {
                Intent i = new Intent(this, BatDauHinhAnhActivity.class);
                startActivity(i);
            });
        }
    }

    private void checkAndShowSuggestion() {
        // Nếu chưa làm bài test khuôn mặt thì mới hiện card gợi ý[cite: 1]
        if (!DataManager.isFaceCompleted(this)) {
            if (cardGoiY != null) cardGoiY.setVisibility(View.VISIBLE);
            txtGoiY.setText("✨ Phân tích giọng nói xong rồi! Bạn hãy thử làm thêm bài 'Phân tích Hình ảnh' để có đánh giá đầy đủ nhất nhé.");
        } else {
            if (cardGoiY != null) cardGoiY.setVisibility(View.GONE);
        }
    }
}