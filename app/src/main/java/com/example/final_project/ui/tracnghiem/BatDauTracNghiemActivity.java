package com.example.final_project.ui.tracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class BatDauTracNghiemActivity extends AppCompatActivity {

    private LinearLayout btnBatDau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_tracnghiem);

        // ánh xạ nút bắt đầu
        btnBatDau = findViewById(R.id.btn_batdautracnghiem);

        // xử lý click
        btnBatDau.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BatDauTracNghiemActivity.this,
                    ChonTracNghiemActivity.class
            );
            startActivity(intent);
        });
    }
}
