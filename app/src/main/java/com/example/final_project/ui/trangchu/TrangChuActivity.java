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
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrangChuActivity extends AppCompatActivity {

    private ImageView btnTracNghiem, btnHinhAnh, imgAvatar, btnHistory;
    private TextView txtHello, txtFinalResult, txtStatusNote, txtMusicSuggest, txtAdvice;

    private String username;
    private AccountRepository repo;

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

        // Các View bổ sung từ đoạn code 2
        txtStatusNote = findViewById(R.id.txtStatusNote);
        txtMusicSuggest = findViewById(R.id.txtMusicSuggest);
        txtAdvice = findViewById(R.id.txtAdvice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserFromServer();
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

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "--/--/----";
        try {
            if (raw.contains("T")) {
                String datePart = raw.split("T")[0];
                String[] parts = datePart.split("-");
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
            return raw;
        } catch (Exception e) { return raw; }
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

        if (txtAdvice != null) {
            txtAdvice.setVisibility(View.VISIBLE);
            txtAdvice.setText("💡 Lời khuyên: " + getAdviceByScore(score));
        }
        if (txtMusicSuggest != null) {
            txtMusicSuggest.setVisibility(View.VISIBLE);
            txtMusicSuggest.setOnClickListener(v -> {
                String url = getMusicLinkByScore(score);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            });
        }
    }

    private void calculateAndSync() {
        boolean hasQuiz = DataManager.isQuizCompleted(this);
        boolean hasVoice = DataManager.isVoiceCompleted(this);
        boolean hasFace = DataManager.isFaceCompleted(this);
        boolean completedAll = hasQuiz && hasVoice && hasFace;

        if (completedAll) {
            // TÍNH TOÁN & LƯU SERVER
            int qScore = DataManager.getQuizScore(this);
            int vRes = DataManager.getVoiceResult(this);
            boolean fRes = DataManager.getFaceResult(this);

            int finalScore = (int) Math.round((0.5*(qScore/24.0) + 0.3*(vRes==1?1:0) + 0.2*(fRes?1:0)) * 24);
            String level = calculateLevel(finalScore);
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            showResult(finalScore, level, today);
            saveResultToServer(finalScore, level, qScore, vRes, fRes);

            // RESET TIẾN TRÌNH LOCAL ĐỂ LÀM LƯỢT MỚI
            DataManager.resetProgressForNewTest(this);
            if (txtStatusNote != null) txtStatusNote.setVisibility(View.GONE);

        } else {
            // HIỂN THỊ KẾT QUẢ CŨ TỪ SERVER HOẶC TRỐNG
            if (serverScore != null) {
                showResult(serverScore, serverLevel, formatDate(lastTestTime));
            } else {
                txtFinalResult.setText("Chưa có kết quả test");
                if (txtAdvice != null) txtAdvice.setVisibility(View.GONE);
                if (txtMusicSuggest != null) txtMusicSuggest.setVisibility(View.GONE);
            }
            // ✅ BỔ SUNG: GỢI Ý CÁC BÀI CHƯA LÀM (Lấy từ đoạn code 2)
            updateStatusSuggestion(hasQuiz, hasVoice, hasFace);
        }
    }

    private void updateStatusSuggestion(boolean hasQuiz, boolean hasVoice, boolean hasFace) {
        if (txtStatusNote == null) return;

        int completedCount = (hasQuiz ? 1 : 0) + (hasVoice ? 1 : 0) + (hasFace ? 1 : 0);
        if (completedCount == 0) {
            txtStatusNote.setVisibility(View.GONE);
            return;
        }

        StringBuilder suggestion = new StringBuilder("✨ Lượt mới: ");
        if (hasFace) {
            if (!hasQuiz && !hasVoice) suggestion.append("Hãy làm bài [Trắc nghiệm] và [Giọng nói] để cập nhật kết quả.");
            else if (!hasQuiz) suggestion.append("Làm thêm bài [Trắc nghiệm] để hoàn tất.");
            else if (!hasVoice) suggestion.append("Làm nốt bài [Giọng nói] để cập nhật kết quả.");
        } else {
            suggestion.append("Bạn nên thực hiện bài [Hình ảnh khuôn mặt] để phân tích đầy đủ.");
        }

        txtStatusNote.setText(suggestion.toString());
        txtStatusNote.setVisibility(View.VISIBLE);
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

    private void saveResultToServer(int finalScore, String level, int q, int v, boolean f) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("finalScore", finalScore);
        body.put("level", level);
        body.put("quizScore", q);
        body.put("voiceResult", v);
        body.put("faceResult", f);

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        body.put("lastTestTime", iso.format(new Date()));

        repo.updateResult(body).enqueue(new Callback<ApiResponse>() {
            @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
            @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
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

    private void setupHistoryButton() {
        btnHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, LichSuActivity.class);
            i.putExtra("username", username);
            if (serverScore != null) {
                i.putExtra("history_score", serverScore);
                i.putExtra("history_level", serverLevel);
                i.putExtra("history_date", formatDate(lastTestTime));
                i.putExtra("detail_quiz", serverQuizScore);
                i.putExtra("detail_voice", serverVoiceResult);
                i.putExtra("detail_face", serverFaceResult);
            }
            startActivity(i);
        });
    }

    private void setupAvatarMenu() {
        imgAvatar.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, imgAvatar);
            menu.getMenuInflater().inflate(R.menu.menu_avatar, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    startActivity(new Intent(this, com.example.final_project.ui.dangnhap.DangNhapActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }
}