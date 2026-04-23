package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.util.DataManager;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnHinhAnh;
    private TextView txtHello, txtFinalResult; // Đã xóa txtStatusNote

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        initViews();

        String username = getIntent().getStringExtra("username");
        if (username != null) {
            txtHello.setText("Chào, " + username);
        }

        setupNavigation();
        calculateAndShowFinalScore();
    }

    private void initViews() {
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);
        txtFinalResult = findViewById(R.id.txtFinalResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateAndShowFinalScore();
    }

    private void calculateAndShowFinalScore() {
        double sQuiz = DataManager.getQuizScore(this) / 24.0;
        double sVoice = (DataManager.getVoiceResult(this) == 1) ? 1.0 : 0.0;
        double sFace = DataManager.getFaceResult(this) ? 1.0 : 0.0;

        double sFinal = (0.50 * sQuiz) + (0.30 * sVoice) + (0.20 * sFace);
        int finalScore = (int) Math.round(sFinal * 24);

        String level;
        if (finalScore <= 4) level = "Bình thường";
        else if (finalScore <= 9) level = "Nhẹ";
        else if (finalScore <= 14) level = "Vừa";
        else if (finalScore <= 19) level = "Nặng vừa";
        else level = "Nặng";

        txtFinalResult.setText(finalScore + " điểm - " + level);
    }

    private void setupNavigation() {
        btnTracNghiem.setOnClickListener(v ->
                startActivity(new Intent(this, BatDauTracNghiemActivity.class)));

        btnHinhAnh.setOnClickListener(v ->
                startActivity(new Intent(this, BatDauHinhAnhActivity.class)));
    }
}