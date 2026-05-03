package com.example.final_project.ui.ketqua;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.final_project.R;
import com.example.final_project.ui.ghiam.BatDauGhiAmActivity;
import com.example.final_project.util.DataManager;

public class KetQuaTracNghiemActivity extends AppCompatActivity {

    private TextView txtKetQua;
    private AppCompatButton btnKetThuc; // Đã đổi từ LinearLayout thành AppCompatButton
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_tracnghiem);

        // Ánh xạ View
        txtKetQua = findViewById(R.id.textketquatracnghiem);
        btnKetThuc = findViewById(R.id.btnketthuctracnghiem);

        // Nhận điểm số từ Intent
        score = getIntent().getIntExtra("score", 0);

        // Lưu điểm vào DataManager để trang Lịch sử có thể sử dụng
        DataManager.saveQuizScore(this, score);

        // Hiển thị kết quả văn bản
        showResult(score);

        // Xử lý sự kiện nút Tiếp tục
        if (btnKetThuc != null) {
            btnKetThuc.setOnClickListener(v -> {
                Toast.makeText(this, "Chuyển sang kiểm tra giọng nói...", Toast.LENGTH_SHORT).show();

                // Chuyển sang phần test tiếp theo là Ghi âm
                Intent intent = new Intent(KetQuaTracNghiemActivity.this, BatDauGhiAmActivity.class);
                startActivity(intent);

                // Kết thúc Activity hiện tại để không quay lại được bằng nút Back
                finish();
            });
        }
    }

    private void showResult(int score) {
        String level;
        if (score <= 4) {
            level = "Trầm cảm tối thiểu";
        } else if (score <= 9) {
            level = "Trầm cảm nhẹ";
        } else if (score <= 14) {
            level = "Trầm cảm trung bình";
        } else {
            level = "Trầm cảm nặng";
        }
        txtKetQua.setText(level);
    }
}