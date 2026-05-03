package com.example.final_project.ui.lichsu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class LichSuActivity extends AppCompatActivity {

    // Các biến lưu trữ dữ liệu được truyền từ Trang chủ
    private int score, detailQuiz, detailVoice;
    private String level, date;
    private boolean detailFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lichsu);

        // 1. Nhận dữ liệu
        receiveData();

        // 2. Thiết lập giao diện
        setupViews();
    }

    private void receiveData() {
        // Nhận dữ liệu tổng hợp
        score = getIntent().getIntExtra("history_score", -1);
        level = getIntent().getStringExtra("history_level");
        date = getIntent().getStringExtra("history_date");

        // Nhận các đầu điểm chi tiết (để truyền tiếp sang trang Chi Tiết)
        detailQuiz = getIntent().getIntExtra("detail_quiz", 0);
        detailVoice = getIntent().getIntExtra("detail_voice", 0);
        detailFace = getIntent().getBooleanExtra("detail_face", false);
    }

    private void setupViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtHistoryScore = findViewById(R.id.txtHistoryScore);
        TextView txtHistoryLevel = findViewById(R.id.txtHistoryLevel);
        TextView txtHistoryDate = findViewById(R.id.txtHistoryDate);
        TextView txtNoData = findViewById(R.id.txtNoData);
        View cardResult = findViewById(R.id.cardResult);

        // Nếu có điểm (score != -1), hiển thị Card lịch sử
        if (score != -1) {
            txtHistoryScore.setText(String.valueOf(score));
            txtHistoryLevel.setText(level != null ? level : "N/A");
            txtHistoryDate.setText("Ngày thực hiện: " + (date != null ? date : "--/--/----"));

            cardResult.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);

            // SỰ KIỆN QUAN TRỌNG: Mở trang Chi Tiết thay vì hiện BottomSheet
            cardResult.setOnClickListener(v -> {
                Intent intent = new Intent(LichSuActivity.this, ChiTietLichSuActivity.class);
                intent.putExtra("total_score", score);
                intent.putExtra("level", level);
                intent.putExtra("date", date);
                intent.putExtra("quiz_score", detailQuiz);
                intent.putExtra("voice_res", detailVoice);
                intent.putExtra("face_res", detailFace);
                startActivity(intent);
            });
        } else {
            // Nếu không có dữ liệu, hiện thông báo trống
            cardResult.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}