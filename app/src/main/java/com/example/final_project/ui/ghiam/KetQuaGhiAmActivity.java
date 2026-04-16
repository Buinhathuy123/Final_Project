package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;
import com.example.final_project.util.DataManager;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtMoTa, txtKetQuaTongHop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        txtMoTa = findViewById(R.id.txtMoTa);
        txtKetQuaTongHop = findViewById(R.id.txtKetQuaCuoiCung);

        Intent intent = getIntent();

        int labelAudio = intent.getIntExtra("label_voice", 0);
        int scoreText = intent.getIntExtra("score_text", 2);
        String resultText = intent.getStringExtra("result_text");

        Log.d("FINAL_DEBUG", "labelAudio = " + labelAudio);
        Log.d("FINAL_DEBUG", "scoreText = " + scoreText);

        calculateAndDisplay(labelAudio, scoreText);

        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            Intent i = new Intent(KetQuaGhiAmActivity.this, TrangChuActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }

    private void calculateAndDisplay(int labelAudio, int scoreText) {
        int scoreAudioEquivalent = (labelAudio == 1) ? 22 : 2;

        int finalScore = (scoreText + scoreAudioEquivalent) / 2;

        String levelFinal;
        int color;
        String advice;

        if (finalScore <= 4) {
            levelFinal = "Bình thường";
            color = android.R.color.holo_green_dark;
            advice = "Tâm trạng ổn định. Hãy duy trì thói quen tích cực.";
        }
        else if (finalScore <= 9) {
            levelFinal = "Nhẹ";
            color = android.R.color.holo_blue_dark;
            advice = "Bạn có dấu hiệu nhẹ. Hãy nghỉ ngơi và thư giãn.";
        }
        else if (finalScore <= 14) {
            levelFinal = "Vừa phải";
            color = android.R.color.holo_orange_dark;
            advice = "Bạn nên chia sẻ với người thân.";
        }
        else if (finalScore <= 19) {
            levelFinal = "Mức độ nghiêm trọng vừa phải";
            color = android.R.color.holo_red_light;
            advice = "Bạn nên gặp chuyên gia tâm lý.";
        }
        else {
            levelFinal = "Nghiêm trọng";
            color = android.R.color.holo_red_dark;
            advice = "Cần hỗ trợ y tế ngay.";
        }

        txtKetQuaTongHop.setText("Kết quả: " + finalScore + " điểm - " + levelFinal);
        txtKetQuaTongHop.setTextColor(ContextCompat.getColor(this, color));
        txtMoTa.setText(advice);

        DataManager.saveTextScore(this, finalScore);
    }
}