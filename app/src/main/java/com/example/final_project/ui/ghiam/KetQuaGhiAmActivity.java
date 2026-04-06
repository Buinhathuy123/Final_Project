package com.example.final_project.ui.ghiam;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.final_project.R;

public class KetQuaGhiAmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ketqua_ghiam);

        TextView txtKetQua = findViewById(R.id.txtKetQua);
        TextView txtMoTa = findViewById(R.id.txtMoTa);

        // Nhận điểm số từ Activity trước
        int score = getIntent().getIntExtra("final_score", 0);
        String tag = getIntent().getStringExtra("result_tag");

        String thongBao = "Kết quả: " + score + " điểm - ";

        // Logic hiển thị 5 mức độ
        if (score <= 4) {
            txtKetQua.setText(thongBao + "Bình thường");
            txtKetQua.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            txtMoTa.setText("Tâm trạng của bạn rất ổn định. Hãy tiếp tục duy trì lối sống lành mạnh nhé!");
        } else if (score <= 9) {
            txtKetQua.setText(thongBao + "Trầm cảm tối thiểu");
            txtKetQua.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            txtMoTa.setText("Bạn có dấu hiệu lo âu nhẹ. Hãy dành thời gian thư giãn và nghỉ ngơi nhiều hơn.");
        } else if (score <= 14) {
            txtKetQua.setText(thongBao + "Trầm cảm nhẹ");
            txtKetQua.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            txtMoTa.setText("Bạn nên chú ý đến sức khỏe tâm thần và chia sẻ nhiều hơn với bạn bè, người thân.");
        } else if (score <= 19) {
            txtKetQua.setText(thongBao + "Trầm cảm trung bình");
            txtKetQua.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            txtMoTa.setText("Mức độ trầm cảm vừa phải. Bạn nên cân nhắc gặp chuyên gia tư vấn tâm lý.");
        } else {
            txtKetQua.setText(thongBao + "Trầm cảm nặng");
            txtKetQua.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            txtMoTa.setText("Cảnh báo mức độ nghiêm trọng. Bạn cần liên hệ với bác sĩ hoặc chuyên gia y tế ngay lập tức.");
        }
    }
}