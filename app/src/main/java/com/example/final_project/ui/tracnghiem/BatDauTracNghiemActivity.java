package com.example.final_project.ui.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;

public class BatDauTracNghiemActivity extends AppCompatActivity {

    private LinearLayout btnBatDau;
    private ImageView btnBack;

    private String username; // 🔥 thêm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_tracnghiem);

        btnBatDau = findViewById(R.id.btn_batdautracnghiem);
        btnBack = findViewById(R.id.btn_back);

        // 🔥 nhận username từ TrangChu
        username = getIntent().getStringExtra("username");

        // =========================
        // START QUIZ
        // =========================
        btnBatDau.setOnClickListener(v -> {

            Intent intent = new Intent(
                    BatDauTracNghiemActivity.this,
                    ChonTracNghiemActivity.class
            );

            // 🔥 truyền tiếp username
            intent.putExtra("username", username);

            startActivity(intent);
        });

        // =========================
        // BACK
        // =========================
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BatDauTracNghiemActivity.this,
                    TrangChuActivity.class
            );

            // 🔥 truyền lại username (để tránh null)
            intent.putExtra("username", username);

            startActivity(intent);
            finish();
        });
    }
}