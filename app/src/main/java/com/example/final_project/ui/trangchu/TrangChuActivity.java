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
import com.example.final_project.ui.lichsu.LichSuActivity;
import com.example.final_project.util.DataManager;

import com.example.final_project.data.repository.AccountRepository;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnHinhAnh, imgAvatar, btnHistory;
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
        setupHistoryButton();

        loadUserFromServer();
    }

    private void initViews() {
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);
        txtFinalResult = findViewById(R.id.txtFinalResult);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnHistory = findViewById(R.id.btnHistory);
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

                calculateAndSync();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                calculateAndSync();
            }
        });
    }

    // =========================
    // FORMAT DATE (FIX CHUẨN)
    // =========================
    private String formatDate(String raw) {

        if (raw == null || raw.isEmpty()) return "--/--/----";

        try {
            Date date;

            if (raw.contains("T")) {
                SimpleDateFormat iso =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                date = iso.parse(raw);
            } else {
                SimpleDateFormat old =
                        new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                date = old.parse(raw);
            }

            SimpleDateFormat output =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            return output.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            return raw; // 🔥 fallback
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
    // MAIN LOGIC
    // =========================
    private void calculateAndSync() {

        // ưu tiên server
        if (serverScore != null && serverLevel != null) {
            showResult(serverScore, serverLevel, formatDate(lastTestTime));
        }

        boolean completedAll = DataManager.isQuizCompleted(this) &&
                DataManager.isVoiceCompleted(this) &&
                DataManager.isFaceCompleted(this);

        if (!completedAll) {
            if (serverScore == null) {
                txtFinalResult.setText("Chưa có kết quả test");
            }
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

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());

        String serverDate = formatDate(lastTestTime);

        boolean isChanged =
                serverScore == null ||
                        serverLevel == null ||
                        finalScore != serverScore ||
                        !level.equals(serverLevel) ||
                        !today.equals(serverDate);

        if (isChanged) {

            showResult(finalScore, level, today);

            saveResultToServer(finalScore, level);

            serverScore = finalScore;
            serverLevel = level;
            lastTestTime = today;
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

        SimpleDateFormat iso =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));

        body.put("lastTestTime", iso.format(new Date()));

        repo.updateResult(body).enqueue(new Callback<ApiResponse>() {
            @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
            @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }

    // =========================
    // NAVIGATION
    // =========================
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

    // =========================
    // HISTORY
    // =========================
    private void setupHistoryButton() {
        btnHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, LichSuActivity.class);
            i.putExtra("username", username);
            startActivity(i);
        });
    }

    // =========================
    // AVATAR MENU
    // =========================
    private void setupAvatarMenu() {

        imgAvatar.setOnClickListener(v -> {

            PopupMenu menu = new PopupMenu(this, imgAvatar);
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