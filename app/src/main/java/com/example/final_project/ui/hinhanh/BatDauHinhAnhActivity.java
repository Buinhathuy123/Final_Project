package com.example.final_project.ui.hinhanh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;

public class BatDauHinhAnhActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batdau_hinhanh);


        View btnBack = findViewById(R.id.rd76jz0yyq7k);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }


        View btnBatDau = findViewById(R.id.rjlhf61uofxf);
        if (btnBatDau != null) {
            btnBatDau.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BatDauHinhAnhActivity.this, ChonHinhHinhAnhActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}