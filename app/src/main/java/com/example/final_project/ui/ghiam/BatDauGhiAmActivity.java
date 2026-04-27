package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;

public class BatDauGhiAmActivity extends AppCompatActivity {

    private LinearLayout btnBatDau;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_ghiam);

        btnBatDau = findViewById(R.id.btn_batdauthuchienghiam);

        btnBack = findViewById(R.id.btn_back);

        btnBatDau.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BatDauGhiAmActivity.this,
                    TrangGhiAmActivity.class
            );
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BatDauGhiAmActivity.this,
                    TrangChuActivity.class
            );
            startActivity(intent);
            finish();
        });
    }
}