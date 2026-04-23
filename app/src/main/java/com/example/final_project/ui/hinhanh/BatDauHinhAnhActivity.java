package com.example.final_project.ui.hinhanh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class BatDauHinhAnhActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_hinhanh);

        // ✅ Nhận username từ TrangChu
        username = getIntent().getStringExtra("username");

        View btnBack = findViewById(R.id.rd76jz0yyq7k);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnBatDau = findViewById(R.id.rjlhf61uofxf);
        if (btnBatDau != null) {
            btnBatDau.setOnClickListener(v -> {
                Intent intent = new Intent(BatDauHinhAnhActivity.this, ChonHinhHinhAnhActivity.class);

                // ✅ Truyền username sang màn tiếp theo
                intent.putExtra("username", username);

                startActivity(intent);
            });
        }
    }
}