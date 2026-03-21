package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class ChonDangNhapActivity extends AppCompatActivity {

    private TextView btndangnhap,btndangky;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        // ánh xạ nút bắt đầu
        btndangnhap = findViewById(R.id.btn_dangnhap);
        btndangky = findViewById(R.id.btn_dangky);

        // xử lý click
        btndangky.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ChonDangNhapActivity.this,
                    DangKyActivity.class
            );
            startActivity(intent);
        });
        btndangnhap.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ChonDangNhapActivity.this,
                    DangNhapActivity.class
            );
            startActivity(intent);
        });
    }
}
