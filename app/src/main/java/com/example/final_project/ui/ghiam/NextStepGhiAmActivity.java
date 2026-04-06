package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.text.TFLiteHelper;

import java.util.ArrayList;
import java.util.Locale;

public class NextStepGhiAmActivity extends AppCompatActivity {

    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnVoice;
    private LinearLayout btnNext;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    private StringBuilder fullTextBuilder = new StringBuilder();
    private String lastRecognizedText = "";
    private String answerFromQuestion1 = ""; // Nhận từ Activity trước

    private Handler handler = new Handler();
    private long startTime;
    private long recordSeconds = 0;
    private static final int MIN_RECORD_TIME = 10;

    private TFLiteHelper tfLiteHelper;
    private final String question2 = "Gần đây bạn có gặp khó khăn trong việc đi vào giấc ngủ, ngủ không sâu giấc hoặc ngủ quá nhiều không?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam); // Dùng chung layout với câu 1

        // 1. Lấy dữ liệu từ Câu 1 truyền sang
        answerFromQuestion1 = getIntent().getStringExtra("answer_question_1");

        // 2. Khởi tạo AI
        tfLiteHelper = new TFLiteHelper(this);

        initViews();
        initSpeech();

        // 3. Hiển thị câu hỏi 2
        txtCauHoi.setText(question2);
        txtKetQuaNoi.setText("Nhấn nút để trả lời câu hỏi 2");

        btnVoice.setOnClickListener(v -> toggleSpeech());
        btnNext.setOnClickListener(v -> finishTest());
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);
        btnVoice = findViewById(R.id.btnbatdaughiam);
        btnNext = findViewById(R.id.btn_cautieptheo);
    }

    private void initSpeech() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        // 🔥 Cấu hình chống ngắt quãng (5 giây im lặng mới dừng)
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Không xóa Text ở đây để tránh bị chớp màn hình khi restart mic
                if (fullTextBuilder.length() == 0) {
                    txtKetQuaNoi.setText("Đang nghe...");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String currentPartial = data.get(0);

                    // 🔥 HIỂN THỊ: Kết hợp Builder (đã nói xong) + Partial (đang nói)
                    String displayText = fullTextBuilder.toString() + " " + currentPartial;
                    txtKetQuaNoi.setText(displayText.trim());

                    // Cập nhật biến tạm để nếu bấm "Tiếp theo" ngay vẫn có dữ liệu
                    lastRecognizedText = displayText.trim();
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String finalSegment = data.get(0);

                    // 🔥 KIỂM TRA TRÙNG LẶP: Tránh việc một câu bị ghi vào Builder 2 lần
                    if (!fullTextBuilder.toString().contains(finalSegment)) {
                        fullTextBuilder.append(finalSegment).append(" ");
                    }

                    lastRecognizedText = fullTextBuilder.toString().trim();
                    txtKetQuaNoi.setText(lastRecognizedText);
                }

                // Tự động nghe tiếp nếu người dùng chưa bấm dừng (isListening vẫn là true)
                if (isListening) {
                    speechRecognizer.startListening(speechIntent);
                }
            }

            @Override
            public void onError(int error) {
                Log.e("SPEECH_FIX", "Error code: " + error);

                // Nếu gặp lỗi (Mã 7: No match, Mã 8: Busy, Mã 6: Timeout)
                if (isListening) {
                    // Sử dụng Handler để restart sau 0.5s để hệ thống kịp giải phóng Mic
                    new Handler(getMainLooper()).postDelayed(() -> {
                        try {
                            speechRecognizer.startListening(speechIntent);
                        } catch (Exception e) {
                            Log.e("SPEECH_FIX", "Restart failed");
                        }
                    }, 500);
                }
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void toggleSpeech() {
        if (!isListening) {
            isListening = true;
            fullTextBuilder.setLength(0);
            btnVoice.setImageResource(R.drawable.dangghiam);
            startTime = System.currentTimeMillis();
            handler.post(timerRunnable);
            speechRecognizer.startListening(speechIntent);
        } else {
            isListening = false;
            btnVoice.setImageResource(R.drawable.nutghiam);
            handler.removeCallbacks(timerRunnable);
            speechRecognizer.stopListening();
            if (!lastRecognizedText.isEmpty()) txtKetQuaNoi.setText(lastRecognizedText);
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            recordSeconds = (System.currentTimeMillis() - startTime) / 1000;
            txtThoiGian.setText(String.format(Locale.getDefault(), "%02d:%02d", recordSeconds / 60, recordSeconds % 60));
            handler.postDelayed(this, 1000);
        }
    };

    private void finishTest() {
        isListening = false;
        speechRecognizer.stopListening();

        String answer2 = lastRecognizedText.trim();
        if (answer2.isEmpty() || answer2.equalsIgnoreCase("Đang nghe câu 2...")) {
            Toast.makeText(this, "Vui lòng trả lời câu 2!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gộp văn bản 2 câu để AI phân tích tổng thể
        String finalCombinedText = answerFromQuestion1 + " " + answer2;
        String result = tfLiteHelper.predict(finalCombinedText);

        int finalScore = 0;
        if (result != null) {
            result = result.toLowerCase();
            // Ánh xạ nhãn sang điểm số trung bình của từng khoảng
            switch (result) {
                case "normal":   finalScore = 2;  break; // Khoảng 0-4
                case "minimal":  finalScore = 7;  break; // Khoảng 5-9
                case "mild":     finalScore = 12; break; // Khoảng 10-14
                case "moderate": finalScore = 17; break; // Khoảng 15-19
                case "severe":   finalScore = 22; break; // Khoảng >19
            }
        }

        Intent intent = new Intent(this, KetQuaGhiAmActivity.class);
        // 🔥 QUAN TRỌNG: Dùng chung Key "final_score" cho tất cả các Activity
        intent.putExtra("final_score", finalScore);
        intent.putExtra("result_tag", result);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }
}