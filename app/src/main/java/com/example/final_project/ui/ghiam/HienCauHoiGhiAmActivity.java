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

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnVoice;
    private LinearLayout btnNext;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    // 🔥 FIX: Dùng StringBuilder để cộng dồn văn bản khi nói dài
    private StringBuilder fullTextBuilder = new StringBuilder();
    private String lastRecognizedText = "";

    private Handler handler = new Handler();
    private long startTime;
    private long recordSeconds = 0;
    private static final int MIN_RECORD_TIME = 10;

    private TFLiteHelper tfLiteHelper;
    private final String question = "Trong 2 tuần gần đây, bạn có thường cảm thấy buồn bã, chán nản hoặc mất hứng thú với mọi việc không?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        // Khởi tạo AI sớm để tránh lag
        tfLiteHelper = new TFLiteHelper(this);

        requestPermission();
        initViews();
        initSpeech();
        showQuestion(); // Đảm bảo câu hỏi hiện ngay từ đầu

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

        // 🔥 FIX 1: Xóa dòng lỗi EXTRA_LANGUAGE (true) cũ và thay bằng cấu hình chuẩn
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        speechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Không xóa Text cũ ở đây để tránh bị chớp màn hình khi restart mic
                if (fullTextBuilder.length() == 0) {
                    txtKetQuaNoi.setText("Đang nghe...");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String currentPartial = data.get(0);

                    // 🔥 FIX 2: Hiển thị kết hợp Builder (đã lưu) + Partial (đang nghe)
                    // Dùng dấu cách để phân tách các đoạn
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

                    // 🔥 FIX 3: Tránh cộng dồn trùng lặp văn bản
                    if (!fullTextBuilder.toString().contains(finalSegment)) {
                        fullTextBuilder.append(finalSegment).append(" ");
                    }

                    lastRecognizedText = fullTextBuilder.toString().trim();
                    txtKetQuaNoi.setText(lastRecognizedText);
                }

                // Tự động nghe tiếp nếu vẫn đang trong chế độ ghi âm
                if (isListening) {
                    speechRecognizer.startListening(speechIntent);
                }
            }

            @Override
            public void onError(int error) {
                Log.e("SPEECH_FIX", "Error code: " + error);

                // Lỗi 7 (No Match) thường xảy ra khi im lặng hoặc mic kém -> Phải restart
                if (isListening) {
                    new Handler(getMainLooper()).postDelayed(() -> {
                        try {
                            speechRecognizer.startListening(speechIntent);
                        } catch (Exception e) {
                            Log.e("SPEECH_FIX", "Restart failed", e);
                        }
                    }, 400);
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
            startListening();
        } else {
            stopListening();
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Chưa cấp quyền mic!", Toast.LENGTH_SHORT).show();
            return;
        }

        isListening = true;
        fullTextBuilder.setLength(0); // Xóa dữ liệu cũ
        lastRecognizedText = "";

        speechRecognizer.startListening(speechIntent);
        setRecordingUI(true);

        startTime = System.currentTimeMillis();
        handler.post(timerRunnable);
    }

    private void stopListening() {
        isListening = false;
        try {
            speechRecognizer.stopListening();
        } catch (Exception ignored) {}

        setRecordingUI(false);
        handler.removeCallbacks(timerRunnable);

        // 🔥 FIX: Khi dừng, đảm bảo hiện text cuối cùng, không hiện "Đang nghe"
        if (!lastRecognizedText.isEmpty()) {
            txtKetQuaNoi.setText(lastRecognizedText);
        } else {
            txtKetQuaNoi.setText("Nhấn nút để trả lời bằng giọng nói");
        }
    }

    private void setRecordingUI(boolean isRecording) {
        btnVoice.setImageResource(isRecording ? R.drawable.dangghiam : R.drawable.nutghiam);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            recordSeconds = (System.currentTimeMillis() - startTime) / 1000;
            txtThoiGian.setText(String.format(Locale.getDefault(), "%02d:%02d", recordSeconds / 60, recordSeconds % 60));
            handler.postDelayed(this, 1000);
        }
    };

    private void showQuestion() {
        txtCauHoi.setText(question);
    }

    private void finishTest() {
        isListening = false;
        speechRecognizer.stopListening();
        handler.removeCallbacks(timerRunnable);

        String answer1 = lastRecognizedText.trim();

        if (answer1.isEmpty() || answer1.equalsIgnoreCase("Đang nghe...")) {
            Toast.makeText(this, "Vui lòng trả lời câu 1!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Truyền văn bản câu 1 sang Activity tiếp theo
        Intent intent = new Intent(this, NextStepGhiAmActivity.class);
        intent.putExtra("answer_question_1", answer1);
        startActivity(intent);
        finish();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        handler.removeCallbacks(timerRunnable);
    }
}