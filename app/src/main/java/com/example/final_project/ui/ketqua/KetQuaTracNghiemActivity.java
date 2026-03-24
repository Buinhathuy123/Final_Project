package com.example.final_project.ui.ketqua;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActivity;

public class KetQuaTracNghiemActivity extends AppCompatActivity {

    private TextView txtKetQua, txtLoiKhuyen, txtGoiYAmNhac;
    private LinearLayout btnKetThuc;

    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_tracnghiem);

        txtKetQua = findViewById(R.id.textketquatracnghiem);
        txtLoiKhuyen = findViewById(R.id.textloikhuyentracnghiem);
        txtGoiYAmNhac = findViewById(R.id.textgoiyamnhac);
        btnKetThuc = findViewById(R.id.btnketthuctracnghiem);

        score = getIntent().getIntExtra("score", 0);
        showResult(score);

        // Finish button
        btnKetThuc.setOnClickListener(v -> {
            Intent intent = new Intent(
                    KetQuaTracNghiemActivity.this,
                    TrangChuActivity.class
            );
            startActivity(intent);
            finish();
        });

        // Open music suggestion
        txtGoiYAmNhac.setOnClickListener(v -> {
            String url = getMusicLinkByScore(score);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    private void showResult(int score) {

        String level;
        String advice;

        if (score <= 4) {

            level = "Trầm cảm tối thiểu";
            advice = "Sức khỏe tinh thần của bạn đang khá ổn định. Hãy tiếp tục duy trì lối sống lành mạnh và giữ kết nối với những người xung quanh.";

        } else if (score <= 9) {

            level = "Trầm cảm nhẹ";
            advice = "Bạn có thể đang trải qua một chút tâm trạng buồn. Hãy thử thư giãn, tập thể dục hoặc chia sẻ với bạn bè và gia đình.";

        } else if (score <= 14) {

            level = "Trầm cảm trung bình";
            advice = "Bạn nên cân nhắc trao đổi với chuyên gia tâm lý để nhận được sự tư vấn và hỗ trợ.";

        } else {

            level = "Trầm cảm nặng";
            advice = "Bạn nên tìm kiếm sự hỗ trợ từ bác sĩ hoặc chuyên gia sức khỏe tâm thần càng sớm càng tốt.";

        }

        txtKetQua.setText(level);
        txtLoiKhuyen.setText(advice);
    }

    private String getMusicLinkByScore(int score) {

        if (score <= 4) {
            return "https://open.spotify.com/playlist/2WLjVJrYUMcNWf8jKRzBpb";
        }
        else if (score <= 9) {
            return "https://open.spotify.com/album/11nFCEpoPyEvcb1ihgiKkK";
        }
        else if (score <= 14) {
            return "https://open.spotify.com/playlist/37i9dQZF1DX3Ogo9pFvBkY";
        }
        else {
            return "https://open.spotify.com/album/5eUCj0ztGDmYXY417P7TGS";
        }
    }
}