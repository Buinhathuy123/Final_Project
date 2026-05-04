package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.ui.tracnghiem.BatDauTracNghiemActivity;
import com.example.final_project.ui.hinhanh.BatDauHinhAnhActivity;
import com.example.final_project.util.DataManager;

import com.example.final_project.data.repository.AccountRepository;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnHinhAnh, imgAvatar;
    private TextView txtHello, txtFinalResult;

    private String username;
    private AccountRepository repo;

    private Integer serverScore = null;
    private String serverLevel = null;
    private String lastTestTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        initViews();

        repo = new AccountRepository();

        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "User";
        }

        txtHello.setText("Chào, " + username);

        setupNavigation();
        setupAvatarMenu();

        loadUserFromServer();
    }

    private void initViews() {
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);
        txtFinalResult = findViewById(R.id.txtFinalResult);
        imgAvatar = findViewById(R.id.imgAvatar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromServer();
    }

    // =========================
    // LOAD USER
    // =========================
    private void loadUserFromServer() {

        repo.getUser(username).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse res = response.body();

                    if (res.isOk() && res.getData() != null) {

                        Account user = res.getData();

                        serverScore = user.getFinalScore();
                        serverLevel = user.getLevel();
                        lastTestTime = user.getLastTestTime();
                    }
                }

                calculateAndSaveLocalResult();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                calculateAndSaveLocalResult();
            }
        });
    }

    // =========================
    // FORMAT DATE (FIX CHUẨN)
    // =========================
    private String formatDate(String raw) {

        if (raw == null) return "";

        try {
            // ISO format
            if (raw.contains("T")) {
                String datePart = raw.split("T")[0];
                String[] parts = datePart.split("-");
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }

            // Mongo format: Sun May 03 2026 ...
            String[] parts = raw.split(" ");
            String day = parts[2];
            String month = convertMonth(parts[1]);
            String year = parts[3];

            return day + "/" + month + "/" + year;

        } catch (Exception e) {
            return raw;
        }
    }

    private String convertMonth(String month) {
        switch (month) {
            case "Jan": return "01";
            case "Feb": return "02";
            case "Mar": return "03";
            case "Apr": return "04";
            case "May": return "05";
            case "Jun": return "06";
            case "Jul": return "07";
            case "Aug": return "08";
            case "Sep": return "09";
            case "Oct": return "10";
            case "Nov": return "11";
            case "Dec": return "12";
            default: return "00";
        }
    }

    // =========================
    // SHOW RESULT
    // =========================
    private void showResult(int score, String level, String date) {

        String line1 = score + " điểm - " + level;
        String line2 = "Ngày test: " + date;

        String fullText = line1 + "\n" + line2;

        SpannableString spannable = new SpannableString(fullText);

        spannable.setSpan(new AbsoluteSizeSpan(22, true),
                0, line1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new AbsoluteSizeSpan(14, true),
                line1.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtFinalResult.setText(spannable);
        txtFinalResult.setTextColor(Color.WHITE);
        txtFinalResult.setTypeface(null, Typeface.BOLD);
    }

    // =========================
    // CALCULATE + SYNC
    // =========================
    private void calculateAndSaveLocalResult() {

        boolean completedAll = DataManager.isQuizCompleted(this) &&
                DataManager.isVoiceCompleted(this) &&
                DataManager.isFaceCompleted(this);

        if (!completedAll) {
            txtFinalResult.setText("Chưa có kết quả test");
            return;
        }

        double sQuiz = DataManager.getQuizScore(this) / 24.0;
        double sVoice = (DataManager.getVoiceResult(this) == 1) ? 1.0 : 0.0;
        double sFace = DataManager.getFaceResult(this) ? 1.0 : 0.0;

        int finalScore = (int) Math.round((0.5 * sQuiz + 0.3 * sVoice + 0.2 * sFace) * 24);

        String level;
        if (finalScore <= 4) level = "Bình thường";
        else if (finalScore <= 9) level = "Nhẹ";
        else if (finalScore <= 14) level = "Vừa";
        else if (finalScore <= 19) level = "Nặng vừa";
        else level = "Nặng";

        boolean isChanged =
                serverScore == null ||
                        serverLevel == null ||
                        finalScore != serverScore ||
                        !level.equals(serverLevel);

        if (isChanged) {

            // ❗ dùng thời gian mới chỉ khi thay đổi
            String now = new Date().toString();

            showResult(finalScore, level, formatDate(now));

            saveResultToServer(finalScore, level);

            serverScore = finalScore;
            serverLevel = level;
            lastTestTime = now;

        } else {

            // ✅ luôn dùng server
            showResult(serverScore, serverLevel, formatDate(lastTestTime));
        }
    }

    // =========================
    // UPDATE SERVER
    // =========================
    private void saveResultToServer(int finalScore, String level) {

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("finalScore", finalScore);
        body.put("level", level);

        // 🔥 FIX: gửi thời gian hiện tại lên server
        body.put("lastTestTime", new Date().toString());

        repo.updateResult(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }

    private void setupNavigation() {

        btnTracNghiem.setOnClickListener(v -> {
            Intent i = new Intent(this, BatDauTracNghiemActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });

        btnHinhAnh.setOnClickListener(v -> {
            Intent i = new Intent(this, BatDauHinhAnhActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });
    }

    private void setupAvatarMenu() {

        imgAvatar.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(this, imgAvatar, 0, 0, R.style.PopupMenuStyle);
            menu.getMenuInflater().inflate(R.menu.menu_avatar, menu.getMenu());

            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                }
                return false;
            });

            menu.show();
        });
    }

    private void logout() {
        startActivity(new Intent(this,
                com.example.final_project.ui.dangnhap.DangNhapActivity.class));
        finish();
    }
}