package com.example.final_project.ui.hinhanh;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.util.DataManager;

public class KetQuaHinhAnhActivity extends AppCompatActivity {

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_hinhanh);

        TextView txtKetQua = findViewById(R.id.rxkmwbhgwgz);
        TextView txtMoTa = findViewById(R.id.rrk64zpie7uj);
        TextView txtGoiY = findViewById(R.id.txtGoiY);
        LinearLayout btnKetThuc = findViewById(R.id.raf9m3etc9f6);
        View btnGoToQuiz = findViewById(R.id.btnGoToQuiz);

        boolean isDepressed = getIntent().getBooleanExtra("isDepressed", false);
        username = getIntent().getStringExtra("username");

        if (isDepressed) {
            txtKetQua.setText("CÓ DẤU HIỆU TRẦM CẢM");
            txtKetQua.setTextColor(Color.parseColor("#FF0000"));
            txtMoTa.setText("Phân tích khuôn mặt cho thấy các biểu hiện của sự mệt mỏi. Bạn nên trò chuyện với bác sĩ để được hỗ trợ.");
        } else {
            txtKetQua.setText("TÂM TRẠNG BÌNH THƯỜNG");
            txtKetQua.setTextColor(Color.parseColor("#008000"));
            txtMoTa.setText("Tình trạng tinh thần của bạn hiện tại khá ổn định thông qua biểu cảm khuôn mặt.");
        }

        DataManager.saveFaceResult(this, isDepressed);

        if (!DataManager.isQuizCompleted(this)) {
            txtGoiY.setText("✨ Để có kết quả chính xác hơn, bạn hãy thử làm thêm bài 'Trắc nghiệm tâm lý' nhé!");
            txtGoiY.setVisibility(View.VISIBLE);
            if (btnGoToQuiz != null) btnGoToQuiz.setVisibility(View.VISIBLE);
        } else {
            txtGoiY.setVisibility(View.GONE);
            if (btnGoToQuiz != null) btnGoToQuiz.setVisibility(View.GONE);
        }

        if (btnGoToQuiz != null) {
            btnGoToQuiz.setOnClickListener(v -> {
                Intent intent = new Intent(KetQuaHinhAnhActivity.this, BatDauTracNghiemActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            });
        }

        btnKetThuc.setOnClickListener(v -> {
            Intent intent = new Intent(KetQuaHinhAnhActivity.this, TrangChuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            intent.putExtra("username", username);

            startActivity(intent);
            finish();
        });
    }
}