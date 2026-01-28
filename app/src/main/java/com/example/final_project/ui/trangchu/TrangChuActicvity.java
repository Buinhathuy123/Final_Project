package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.ghiam.BatDauGhiAmActivity;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.ui.tracnghiem.ChonTracNghiemActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;

public class TrangChuActicvity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnGhiAm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // Ánh xạ view
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnGhiAm = findViewById(R.id.btn_ghiam);
        // Click chuyển sang màn hình trắc nghiệm
        btnTracNghiem.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrangChuActicvity.this,
                    BatDauTracNghiemActivity.class
            );
            startActivity(intent);
        });
        btnGhiAm.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TrangChuActicvity.this,
                    BatDauGhiAmActivity.class
            );
            startActivity(intent);
        });
        View btnHinhAnh = findViewById(R.id.btn_hinhanh);

        // 3. Bắt sự kiện click
        if (btnHinhAnh != null) {
            btnHinhAnh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TrangChuActicvity.this, BatDauHinhAnhActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (btnHinhAnh != null) {
            btnHinhAnh.setOnClickListener(v -> {
                // Chuyển sang màn hình Bắt đầu Hình ảnh
                Intent intent = new Intent(TrangChuActicvity.this, BatDauHinhAnhActivity.class);
                startActivity(intent);
            });
        }



    }
}
