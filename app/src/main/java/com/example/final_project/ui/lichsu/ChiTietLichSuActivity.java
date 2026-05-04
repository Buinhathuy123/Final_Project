package com.example.final_project.ui.lichsu;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;

public class ChiTietLichSuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitiet_lichsu);

        // 1. Ánh xạ View
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtTotalScore = findViewById(R.id.txtDetailTotalScore);
        TextView txtLevel = findViewById(R.id.txtDetailLevel);
        TextView txtFullDate = findViewById(R.id.txtDetailFullDate);
        TextView txtQuiz = findViewById(R.id.txtDetailQuiz);
        TextView txtVoice = findViewById(R.id.txtDetailVoice);
        TextView txtFace = findViewById(R.id.txtDetailFace);

        // 2. Nhận dữ liệu từ Intent truyền sang
        int totalScore = getIntent().getIntExtra("total_score", 0);
        String level = getIntent().getStringExtra("level");
        String date = getIntent().getStringExtra("date");
        int quizScore = getIntent().getIntExtra("quiz_score", 0);
        int voiceRes = getIntent().getIntExtra("voice_res", 0);
        boolean faceRes = getIntent().getBooleanExtra("face_res", false);

        // 3. Hiển thị thông tin tổng hợp
        txtTotalScore.setText(String.valueOf(totalScore));
        txtLevel.setText("Mức độ: " + (level != null ? level : "N/A"));
        txtFullDate.setText("Ngày thực hiện: " + (date != null ? date : "--/--/----"));

        // 4. Hiển thị thông tin chi tiết từng phần
        txtQuiz.setText(quizScore + " / 24");

        // Hiển thị kết quả Voice
        if (voiceRes == 1) {
            txtVoice.setText("Có dấu hiệu");
            txtVoice.setTextColor(Color.RED);
        } else {
            txtVoice.setText("Bình thường");
            txtVoice.setTextColor(Color.parseColor("#1E293B"));
        }

        // Hiển thị kết quả Face
        if (faceRes) {
            txtFace.setText("Có dấu hiệu");
            txtFace.setTextColor(Color.RED);
        } else {
            txtFace.setText("Bình thường");
            txtFace.setTextColor(Color.parseColor("#1E293B"));
        }

        // 5. Sự kiện quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}