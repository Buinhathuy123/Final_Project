package com.example.final_project.ui.ghiam;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import android.content.Intent;
import android.view.View;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtKetQuaAudio, txtKetQuaText, txtMoTa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            Intent intent = new Intent(
                    KetQuaGhiAmActivity.this,
                    BatDauGhiAmActivity.class
            );

            startActivity(intent);
            finish(); // 🔥 đóng màn kết quả
        });
        // ================= UI =================
        txtKetQuaAudio = findViewById(R.id.txtKetQua1); // audio
        txtKetQuaText = findViewById(R.id.txtKetQua);   // text
        txtMoTa = findViewById(R.id.txtMoTa);

        // ================= NHẬN DATA =================
        int label = getIntent().getIntExtra("label", 0); // audio
        int score = getIntent().getIntExtra("final_score", 0); // text

        // ================= AUDIO RESULT =================
        if (label == 1) {
            txtKetQuaAudio.setText("Có dấu hiệu trầm cảm");
            txtKetQuaAudio.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );
        } else {
            txtKetQuaAudio.setText("Không có dấu hiệu trầm cảm");
            txtKetQuaAudio.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
            );
        }

        // ================= TEXT RESULT =================
        String thongBao = score + " điểm - ";

        if (score <= 4) {
            txtKetQuaText.setText(thongBao + "Bình thường");
            txtKetQuaText.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
            );

            txtMoTa.setText("Tâm trạng của bạn rất ổn định. Hãy tiếp tục duy trì lối sống lành mạnh nhé!");

        } else if (score <= 9) {
            txtKetQuaText.setText(thongBao + "Trầm cảm tối thiểu");
            txtKetQuaText.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );

            txtMoTa.setText("Bạn có dấu hiệu lo âu nhẹ. Hãy dành thời gian thư giãn và nghỉ ngơi nhiều hơn.");

        } else if (score <= 14) {
            txtKetQuaText.setText(thongBao + "Trầm cảm nhẹ");
            txtKetQuaText.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );

            txtMoTa.setText("Bạn nên chú ý đến sức khỏe tâm thần và chia sẻ nhiều hơn với bạn bè, người thân.");

        } else if (score <= 19) {
            txtKetQuaText.setText(thongBao + "Trầm cảm trung bình");
            txtKetQuaText.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );

            txtMoTa.setText("Mức độ trầm cảm vừa phải. Bạn nên cân nhắc gặp chuyên gia tư vấn tâm lý.");

        } else {
            txtKetQuaText.setText(thongBao + "Trầm cảm nặng");
            txtKetQuaText.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );

            txtMoTa.setText("Cảnh báo mức độ nghiêm trọng. Bạn cần liên hệ với bác sĩ hoặc chuyên gia y tế ngay lập tức.");
        }
    }
}