package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.ghiam.BatDauGhiAmActivity;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnGhiAm, btnHinhAnh;
    private TextView txtHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // Ánh xạ view
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnGhiAm = findViewById(R.id.btn_ghiam);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);

        // Nhận username từ Intent
        String username = getIntent().getStringExtra("username");

        if (username != null) {
            txtHello.setText("Hi, " + username);
        }

        // Click sang màn hình Trắc nghiệm
        btnTracNghiem.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrangChuActivity.this,
                    BatDauTracNghiemActivity.class
            );
            startActivity(intent);
        });

        // Click sang màn hình Ghi âm
        btnGhiAm.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrangChuActivity.this,
                    BatDauGhiAmActivity.class
            );
            startActivity(intent);
        });

        // Click sang màn hình Hình ảnh
        btnHinhAnh.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrangChuActivity.this,
                    BatDauHinhAnhActivity.class
            );
            startActivity(intent);
        });
    }
}