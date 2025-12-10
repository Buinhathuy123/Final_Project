package com.example.final_project.ui.ketqua;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;

public class KetQuaTracNghiemActivity extends AppCompatActivity {

    private TextView txtKetQua, txtLoiKhuyen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_tracnghiem);

        txtKetQua = findViewById(R.id.textketquatracnghiem);
        txtLoiKhuyen = findViewById(R.id.textloikhuyentracnghiem);

        int score = getIntent().getIntExtra("score", 0);

        showResult(score);
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
}
