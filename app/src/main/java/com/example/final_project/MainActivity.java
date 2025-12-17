package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.ui.ketqua.KetQuaTracNghiemActivity;
import com.example.final_project.ui.trangchu.TrangChuActicvity;

public class MainActivity extends AppCompatActivity {
    private ImageView btnBatdau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnBatdau = findViewById(R.id.btnbatdau);
        btnBatdau.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    TrangChuActicvity.class
            );
            startActivity(intent);
            finish(); // đóng màn hình kết quả
        });
    }
}