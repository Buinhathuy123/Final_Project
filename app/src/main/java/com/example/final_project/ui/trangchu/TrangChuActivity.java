package com.example.final_project.ui.trangchu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnHinhAnh, imgAvatar, btnHistory;
    private TextView txtHello, txtFinalResult, txtStatusNote, txtMusicSuggest, txtAdvice;

    private String username;
    private AccountRepository repo;

    // Biến lưu trữ kết quả
    private Integer serverScore = null;
    private String serverLevel = null;
    private String lastTestTime = null;
    private int serverQuizScore = 0;
    private int serverVoiceResult = 0;
    private boolean serverFaceResult = false;

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

        // BƯỚC 1: Hiển thị ngay kết quả cuối cùng đã lưu ở máy (nếu có) để tránh màn hình trống
        loadLocalResultOnStart();

        // BƯỚC 2: Gọi server để lấy dữ liệu mới nhất
        loadUserFromServer();
    }

    private void initViews() {
        btnTracNghiem = findViewById(R.id.btn_tracnghiem);
        btnHinhAnh = findViewById(R.id.btn_hinhanh);
        txtHello = findViewById(R.id.txtHello);
        txtFinalResult = findViewById(R.id.txtFinalResult);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtStatusNote = findViewById(R.id.txtStatusNote);
        btnHistory = findViewById(R.id.btnHistory);
        txtMusicSuggest = findViewById(R.id.txtMusicSuggest);
        txtAdvice = findViewById(R.id.txtAdvice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromServer();
    }

    // Hàm lấy dữ liệu đã lưu trong SharedPreferences khi vừa mở App
    private void loadLocalResultOnStart() {
        int lastScore = DataManager.getLastScore(this);
        if (lastScore != -1) {
            serverScore = lastScore;
            serverLevel = DataManager.getLastLevel(this);
            lastTestTime = "Vừa xong"; // Hoặc lưu thêm ngày vào DataManager
            serverQuizScore = DataManager.getLastQuiz(this);
            serverVoiceResult = DataManager.getLastVoice(this);
            serverFaceResult = DataManager.getLastFace(this);

            showResult(serverScore, serverLevel, lastTestTime);
        }
    }

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
                        serverQuizScore = user.getQuizScore();
                        serverVoiceResult = user.getVoiceResult();
                        serverFaceResult = user.isFaceResult();

                        // Cập nhật lại SharedPreferences từ Server cho đồng bộ
                        DataManager.saveLastFullResult(TrangChuActivity.this,
                                serverScore, serverLevel, serverQuizScore, serverVoiceResult, serverFaceResult);
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

    private void calculateAndSaveLocalResult() {
        boolean completedAllLocal = DataManager.isQuizCompleted(this) &&
                DataManager.isVoiceCompleted(this) &&
                DataManager.isFaceCompleted(this);

        if (completedAllLocal) {
            int localQuiz = DataManager.getQuizScore(this);
            int localVoice = DataManager.getVoiceResult(this);
            boolean localFace = DataManager.getFaceResult(this);

            double sQuiz = localQuiz / 24.0;
            double sVoice = (localVoice == 1) ? 1.0 : 0.0;
            double sFace = localFace ? 1.0 : 0.0;

            double sFinal = (0.50 * sQuiz) + (0.30 * sVoice) + (0.20 * sFace);
            int finalScore = (int) Math.round(sFinal * 24);
            String level = calculateLevel(finalScore);
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Lưu vào SharedPreferences (Kết quả cuối cùng)
            DataManager.saveLastFullResult(this, finalScore, level, localQuiz, localVoice, localFace);

            serverScore = finalScore;
            serverLevel = level;
            lastTestTime = today;
            serverQuizScore = localQuiz;
            serverVoiceResult = localVoice;
            serverFaceResult = localFace;

            showResult(finalScore, level, today);
            saveResultToServer(finalScore, level, localQuiz, localVoice, localFace);

            DataManager.resetProgressForNewTest(this);
            txtStatusNote.setVisibility(View.GONE);

        } else {
            if (serverScore != null) {
                showResult(serverScore, serverLevel, formatDate(lastTestTime));
            } else {
                txtFinalResult.setText("Chưa có kết quả test");
                txtMusicSuggest.setVisibility(View.GONE);
                txtAdvice.setVisibility(View.GONE);
            }
            updateStatusSuggestion();
        }
    }

    private void setupNavigation() {
        btnTracNghiem.setOnClickListener(v -> {
            Intent intent = new Intent(this, BatDauTracNghiemActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnHinhAnh.setOnClickListener(v -> {
            Intent intent = new Intent(this, BatDauHinhAnhActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, LichSuActivity.class);
            if (serverScore != null) {
                intent.putExtra("history_score", serverScore);
                intent.putExtra("history_level", serverLevel);
                intent.putExtra("history_date", formatDate(lastTestTime));
                intent.putExtra("detail_quiz", serverQuizScore);
                intent.putExtra("detail_voice", serverVoiceResult);
                intent.putExtra("detail_face", serverFaceResult);
            } else {
                intent.putExtra("history_score", -1);
            }
            startActivity(intent);
        });
    }

    private void showResult(int score, String level, String date) {
        String line1 = score + " điểm - " + level;
        String line2 = "Ngày test: " + date;
        String fullText = line1 + "\n" + line2;

        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new AbsoluteSizeSpan(22, true), 0, line1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(14, true), line1.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtFinalResult.setText(spannable);
        txtFinalResult.setTextColor(Color.WHITE);
        txtFinalResult.setTypeface(null, Typeface.BOLD);

        txtAdvice.setVisibility(View.VISIBLE);
        txtAdvice.setText("💡 Lời khuyên: " + getAdviceByScore(score));

        txtMusicSuggest.setVisibility(View.VISIBLE);
        txtMusicSuggest.setOnClickListener(v -> {
            String url = getMusicLinkByScore(score);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
    }

    private void saveResultToServer(int finalScore, String level, int quiz, int voice, boolean face) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("finalScore", finalScore);
        body.put("level", level);
        body.put("quizScore", quiz);
        body.put("voiceResult", voice);
        body.put("faceResult", face);

        repo.updateResult(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {}
        });
    }

    private String calculateLevel(int score) {
        if (score <= 4) return "Bình thường";
        else if (score <= 9) return "Nhẹ";
        else if (score <= 14) return "Vừa";
        else if (score <= 19) return "Nặng vừa";
        else return "Nặng";
    }

    private String getAdviceByScore(int score) {
        if (score <= 4) return "Sức khỏe tinh thần ổn định. Hãy duy trì lối sống lành mạnh.";
        if (score <= 9) return "Bạn đang có dấu hiệu mệt mỏi nhẹ. Hãy nghỉ ngơi và chia sẻ cùng bạn bè.";
        if (score <= 14) return "Bạn nên cân nhắc trao đổi với chuyên gia tâm lý để được tư vấn.";
        return "Hãy tìm kiếm sự hỗ trợ từ chuyên gia sức khỏe tâm thần càng sớm càng tốt.";
    }

    private String getMusicLinkByScore(int score) {
        if (score <= 4) return "https://open.spotify.com/playlist/2WLjVJrYUMcNWf8jKRzBpb";
        if (score <= 9) return "https://open.spotify.com/album/11nFCEpoPyEvcb1ihgiKkK";
        if (score <= 14) return "https://open.spotify.com/playlist/37i9dQZF1DX3Ogo9pFvBkY";
        return "https://open.spotify.com/album/5eUCj0ztGDmYXY417P7TGS";
    }

    private String formatDate(String raw) {
        if (raw == null) return "";
        try {
            if (raw.contains("T")) {
                String[] parts = raw.split("T")[0].split("-");
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
            return raw;
        } catch (Exception e) { return raw; }
    }

    private void updateStatusSuggestion() {
        boolean hasQuiz = DataManager.isQuizCompleted(this);
        boolean hasVoice = DataManager.isVoiceCompleted(this);
        boolean hasFace = DataManager.isFaceCompleted(this);
        int completedCount = (hasQuiz ? 1 : 0) + (hasVoice ? 1 : 0) + (hasFace ? 1 : 0);
        if (completedCount == 0) { txtStatusNote.setVisibility(View.GONE); return; }
        StringBuilder missing = new StringBuilder();
        if (!hasQuiz) missing.append("Trắc nghiệm, ");
        if (!hasVoice) missing.append("Giọng nói, ");
        if (!hasFace) missing.append("Hình ảnh, ");
        if (missing.length() > 0) {
            String msg = missing.substring(0, missing.length() - 2);
            txtStatusNote.setText("✨ Lượt test mới: Làm thêm [" + msg + "] để cập nhật kết quả.");
            txtStatusNote.setVisibility(View.VISIBLE);
        }
    }

    private void setupAvatarMenu() {
        imgAvatar.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(TrangChuActivity.this, imgAvatar);
            popupMenu.getMenuInflater().inflate(R.menu.menu_avatar, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    Intent intent = new Intent(this, com.example.final_project.ui.dangnhap.DangNhapActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }
}