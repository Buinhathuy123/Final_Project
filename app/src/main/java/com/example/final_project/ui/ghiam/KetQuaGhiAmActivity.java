package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.util.DataManager;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtMoTa, txtKetQuaTongHop, txtGoiY;
    private View btnGoToFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        txtMoTa = findViewById(R.id.txtMoTa);
        txtKetQuaTongHop = findViewById(R.id.txtKetQuaCuoiCung);
        txtGoiY = findViewById(R.id.txtGoiY);
        btnGoToFace = findViewById(R.id.btnGoToFace);

        Intent intent = getIntent();
        int labelAudio = intent.getIntExtra("label_voice", 0);

        //  Logic hiển thị
        if (labelAudio == 1) {
            txtKetQuaTongHop.setText("Có dấu hiệu trầm cảm");
            txtKetQuaTongHop.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );
            txtMoTa.setText("Bạn nên trò chuyện với bác sĩ hoặc chuyên gia tâm lý");
        } else {
            txtKetQuaTongHop.setText("Không có dấu hiệu trầm cảm");
            txtKetQuaTongHop.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
            );
            txtMoTa.setText("Tình trạng tinh thần của bạn hiện tại khá ổn định");
        }

        // Lưu kết quả voice
        DataManager.saveVoiceResult(this, labelAudio);

        // Gợi ý làm thêm phân tích ảnh
        checkAndShowSuggestion();

        // Nút về trang chủ
        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            Intent i = new Intent(KetQuaGhiAmActivity.this, TrangChuActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        // Nút chuyển sang phân tích khuôn mặt
        btnGoToFace.setOnClickListener(v -> {
            Intent i = new Intent(KetQuaGhiAmActivity.this, BatDauHinhAnhActivity.class);
            startActivity(i);
        });
    }

    // Hàm gợi ý multimodal
    private void checkAndShowSuggestion() {
        if (!DataManager.isFaceCompleted(this)) {
            txtGoiY.setText("✨ Phân tích giọng nói xong rồi! Bạn hãy thử làm thêm bài 'Phân tích Hình ảnh' để có đánh giá đầy đủ nhất nhé.");
            txtGoiY.setVisibility(View.VISIBLE);
            btnGoToFace.setVisibility(View.VISIBLE);
        } else {
            txtGoiY.setVisibility(View.GONE);
            btnGoToFace.setVisibility(View.GONE);
        }
    }
}