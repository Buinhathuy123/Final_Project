package com.example.final_project.ui.ghiam;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;

public class KetQuaGhiAmActivity extends AppCompatActivity {

    private TextView txtKetQua, txtMoTa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        txtKetQua = findViewById(R.id.txtKetQua);
        txtMoTa = findViewById(R.id.txtMoTa);

        int label = getIntent().getIntExtra("label", 0);

        if (label == 1) {
            txtKetQua.setText("Có dấu hiệu trầm cảm");
            txtMoTa.setText("Bạn nên trò chuyện với bác sĩ hoặc chuyên gia tâm lý");
        } else {
            txtKetQua.setText("Không có dấu hiệu trầm cảm");
            txtMoTa.setText("Tình trạng tinh thần của bạn hiện tại khá ổn định");
        }
    }
}
