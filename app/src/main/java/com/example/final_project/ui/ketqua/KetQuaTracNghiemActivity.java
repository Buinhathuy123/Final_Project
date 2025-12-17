package com.example.final_project.ui.ketqua;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.trangchu.TrangChuActicvity;

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

        // 👉 Click kết thúc
        btnKetThuc.setOnClickListener(v -> {
            Intent intent = new Intent(
                    KetQuaTracNghiemActivity.this,
                    TrangChuActicvity.class
            );
            startActivity(intent);
            finish();
        });

        // 👉 Click mở link âm nhạc theo mức độ
        txtGoiYAmNhac.setOnClickListener(v -> {
            String url = getMusicLinkByScore(score);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    private void showResult(int score) {
        String mucDo;
        String khuyen;

        if (score <= 4) {
            mucDo = "Không trầm cảm";
            khuyen = "Bạn hãy duy trì lối sống lành mạnh và trò chuyện với người thân khi cần.";
        } else if (score <= 9) {
            mucDo = "Trầm cảm nhẹ";
            khuyen = "Bạn hãy duy trì lối sống lành mạnh và trò chuyện với người thân khi cần.";
        } else if (score <= 14) {
            mucDo = "Trầm cảm trung bình";
            khuyen = "Bạn nên trò chuyện với bác sĩ hoặc chuyên gia sức khoẻ tâm thần.";
        } else {
            mucDo = "Trầm cảm nặng";
            khuyen = "Bạn nên trò chuyện với bác sĩ hoặc chuyên gia sức khoẻ tâm thần.";
        }

        txtKetQua.setText(mucDo);
        txtLoiKhuyen.setText(khuyen);
    }

    // 🔗 Link âm nhạc theo mức độ (bạn thay link tùy ý)
    private String getMusicLinkByScore(int score) {

        if (score <= 4) {
            return "https://open.spotify.com/playlist/2WLjVJrYUMcNWf8jKRzBpb";
        } else if (score <= 9) {
            return "https://open.spotify.com/album/11nFCEpoPyEvcb1ihgiKkK";
        } else if (score <= 14) {
            return "https://open.spotify.com/playlist/37i9dQZF1DX3Ogo9pFvBkY?si=8c7e9d7dfbde4f6b&nd=1&dlsi=9f08d478eba144df";
        } else {
            return "https://open.spotify.com/album/5eUCj0ztGDmYXY417P7TGS";
        }
    }
}
