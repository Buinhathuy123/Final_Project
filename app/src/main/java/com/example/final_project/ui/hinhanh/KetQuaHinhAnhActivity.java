package com.example.final_project.ui.hinhanh;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.util.DataManager;

public class KetQuaHinhAnhActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_hinhanh);

        // 1. Ánh xạ View chính xác kiểu dữ liệu để tránh crash
        TextView txtKetQua = findViewById(R.id.rxkmwbhgwgz);
        TextView txtGoiY = findViewById(R.id.txtGoiY);
        View cardGoiY = findViewById(R.id.cardGoiY); // Thêm ánh xạ Card
        View btnKetThuc = findViewById(R.id.raf9m3etc9f6);
        View btnGoToQuiz = findViewById(R.id.btnGoToQuiz);

        // 2. Nhận dữ liệu
        boolean isDepressed = getIntent().getBooleanExtra("isDepressed", false);
        username = getIntent().getStringExtra("username");

        // 3. Hiển thị kết quả với màu sắc tối ưu
        if (isDepressed) {
            txtKetQua.setText("CÓ DẤU HIỆU TRẦM CẢM");
            txtKetQua.setTextColor(Color.parseColor("#E11D48")); // Màu đỏ Rose đậm chất UI hơn
        } else {
            txtKetQua.setText("TÂM TRẠNG BÌNH THƯỜNG");
            txtKetQua.setTextColor(Color.parseColor("#10B981")); // Màu xanh Emerald
        }

        // Lưu kết quả vào hệ thống[cite: 1]
        DataManager.saveFaceResult(this, isDepressed);

        // 4. Logic hiển thị gợi ý làm trắc nghiệm[cite: 1]
        if (!DataManager.isQuizCompleted(this)) {
            if (cardGoiY != null) cardGoiY.setVisibility(View.VISIBLE);
            txtGoiY.setText("✨ Để có kết quả chính xác hơn, bạn hãy thử làm thêm bài 'Trắc nghiệm tâm lý' nhé!");
        } else {
            if (cardGoiY != null) cardGoiY.setVisibility(View.GONE);
        }

        // 5. Sự kiện Click
        if (btnGoToQuiz != null) {
            btnGoToQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(this, BatDauTracNghiemActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            });
        }

        btnKetThuc.setOnClickListener(v -> {
            Intent intent = new Intent(this, TrangChuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }
}