package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class BatDauGhiAmActivity extends AppCompatActivity {

    private LinearLayout btnBatDau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_ghiam);

        // ánh xạ nút bắt đầu
        btnBatDau = findViewById(R.id.btn_batdauthuchienghiam);

        // xử lý click
        btnBatDau.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BatDauGhiAmActivity.this,
                    HienCauHoiGhiAmActivity.class
            );
            startActivity(intent);
        });
    }
}
