package com.example.final_project.ui.lichsu;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChiTietLichSuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitiet_lichsu);

        // =========================
        // 1. INIT VIEW
        // =========================
        ImageView btnBack = findViewById(R.id.btnBack);

        TextView txtTotalScore = findViewById(R.id.txtDetailTotalScore);
        TextView txtLevel = findViewById(R.id.txtDetailLevel);
        TextView txtFullDate = findViewById(R.id.txtDetailFullDate);

        TextView txtQuiz = findViewById(R.id.txtDetailQuiz);
        TextView txtVoice = findViewById(R.id.txtDetailVoice);
        TextView txtFace = findViewById(R.id.txtDetailFace);

        // =========================
        // 2. GET DATA
        // =========================
        int totalScore = getIntent().getIntExtra("total_score", 0);
        String level = getIntent().getStringExtra("level");
        String rawDate = getIntent().getStringExtra("date");

        int quizScore = getIntent().getIntExtra("quiz_score", 0);
        int voiceRes = getIntent().getIntExtra("voice_res", 0);
        boolean faceRes = getIntent().getBooleanExtra("face_res", false);

        // =========================
        // 3. FORMAT DATE
        // =========================
        String formattedDate = formatDate(rawDate);

        // =========================
        // 4. SET DATA UI
        // =========================
        txtTotalScore.setText(String.valueOf(totalScore));

        txtLevel.setText("Mức độ: " + (level != null ? level : "N/A"));

        txtFullDate.setText("Ngày thực hiện: " +
                (formattedDate.isEmpty() ? "--/--/----" : formattedDate));

        txtQuiz.setText(quizScore + " / 24");

        // =========================
        // 5. VOICE RESULT
        // =========================
        if (voiceRes == 1) {
            txtVoice.setText("Có dấu hiệu");
            txtVoice.setTextColor(Color.RED);
        } else {
            txtVoice.setText("Bình thường");
            txtVoice.setTextColor(Color.parseColor("#1E293B"));
        }

        // =========================
        // 6. FACE RESULT
        // =========================
        if (faceRes) {
            txtFace.setText("Có dấu hiệu");
            txtFace.setTextColor(Color.RED);
        } else {
            txtFace.setText("Bình thường");
            txtFace.setTextColor(Color.parseColor("#1E293B"));
        }

        // =========================
        // 7. BACK
        // =========================
        btnBack.setOnClickListener(v -> finish());
    }

    // =========================
    // FORMAT DATE (ISO → dd/MM/yyyy)
    // =========================
    private String formatDate(String raw) {

        if (raw == null || raw.isEmpty()) return "";

        try {
            Date date;

            // ISO từ server
            if (raw.contains("T")) {
                SimpleDateFormat iso =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                date = iso.parse(raw);
            } else {
                // fallback nếu đã là dạng thường
                return raw;
            }

            SimpleDateFormat output =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            return output.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return raw;
        }
    }
}