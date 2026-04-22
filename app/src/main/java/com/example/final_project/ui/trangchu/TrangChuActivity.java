package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.graphics.Color;
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
    private TextView txtHello, txtFinalResult, txtStatusNote;

    // ⭐ LƯU USERNAME ĐỂ KHÔNG MẤT KHI QUAY LẠI
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        initViews();

        // ✔ lấy username từ intent
        username = getIntent().getStringExtra("username");

        // ✔ nếu null thì giữ lại giá trị cũ (nếu có)
        if (username == null) {
            username = "User";
        }

        txtHello.setText("Chào, " + username);

        setupNavigation();
        calculateAndShowFinalScore();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // ✔ khi quay về từ màn khác
        String newUsername = intent.getStringExtra("username");

        if (newUsername != null && !newUsername.isEmpty()) {
            username = newUsername;
            txtHello.setText("Chào, " + username);
        }
    }

    private void initViews() {
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);
        txtFinalResult = findViewById(R.id.txtFinalResult);
        txtStatusNote = findViewById(R.id.txtStatusNote);
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

        StringBuilder missingTests = new StringBuilder();

        if (!DataManager.isQuizCompleted(this)) missingTests.append("Trắc nghiệm, ");
        if (DataManager.getVoiceResult(this) == -1) missingTests.append("Giọng nói, ");
        if (!DataManager.isFaceCompleted(this)) missingTests.append("Khuôn mặt, ");

        if (missingTests.length() > 0) {
            String msg = missingTests.substring(0, missingTests.length() - 2);
            txtStatusNote.setText("✨ Để chính xác hơn, bạn hãy làm thêm bài: " + msg + " nhé!");
        } else {
            txtStatusNote.setText("🌿 Tuyệt vời! Bạn đã hoàn thành tất cả bài kiểm tra.");
        }

        txtStatusNote.setTextColor(Color.WHITE);
        txtStatusNote.setVisibility(TextView.VISIBLE);
    }

    private void setupNavigation() {
        btnTracNghiem.setOnClickListener(v ->
                startActivity(new Intent(this, BatDauTracNghiemActivity.class)));

        btnHinhAnh.setOnClickListener(v ->
                startActivity(new Intent(this, BatDauHinhAnhActivity.class)));
    }
}