package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.final_project.R;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.repository.QuestionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    // UI
    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnGhiAm;
    private LinearLayout btnCauTiepTheo;

    // Question
    private List<Question> questions;
    private int currentIndex = 0;

    // Speech
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isRecording = false;

    // Timer
    private Handler handler = new Handler();
    private long startTime;
    private DepressionClassifier classifier;
    private Translator vietToEngTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);


        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET
                },
                1
        );
        setupTranslator();
        initViews();
        classifier = new DepressionClassifier(this);
        setupSpeechRecognizer();
        loadQuestions();

        btnGhiAm.setOnClickListener(v -> toggleRecording());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    // ================= INIT =================

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);
        btnGhiAm = findViewById(R.id.btnbatdaughiam);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);

        txtKetQuaNoi.setText("");
        txtThoiGian.setText("00:00");
    }

    // ================= QUESTIONS =================

    private void loadQuestions() {
        new QuestionRepository().loadRandomQuestions(new QuestionRepository.QuestionCallback() {
            @Override
            public void onSuccess(List<Question> randomQuestions) {
                questions = randomQuestions;
                showQuestion();
            }

            @Override
            public void onFail(String error) {
                txtCauHoi.setText("Lỗi tải câu hỏi");
            }
        });
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty()) return;

        txtCauHoi.setText(questions.get(currentIndex).getText());
        txtKetQuaNoi.setText("");
        resetTimer();
    }

    private void nextQuestion() {
        if (isRecording) {
            Toast.makeText(this, "Hãy dừng ghi âm trước", Toast.LENGTH_SHORT).show();
            return;
        }

        currentIndex++;
        if (currentIndex >= questions.size()) {
            Toast.makeText(this, "Đã hết câu hỏi", Toast.LENGTH_SHORT).show();
            return;
        }
        showQuestion();
    }

    // ================= SPEECH =================

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onEvent(int eventType, Bundle params) {}

            @Override
            public void onError(int error) {
                // Không toast lỗi khi stopListening
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> list =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    txtKetQuaNoi.setText(list.get(0));
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> list =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    txtKetQuaNoi.setText(list.get(0));
                }
            }
        });
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    private void setupTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();
        vietToEngTranslator = Translation.getClient(options);

        // Tải model ngôn ngữ nếu chưa có
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        vietToEngTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    // Tải thành công hoặc đã có sẵn
                    // Có thể Toast báo "Đã sẵn sàng dịch" nếu thích
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải gói ngôn ngữ", Toast.LENGTH_SHORT).show();
                });
    }

    private void startRecording() {
        isRecording = true;
        btnGhiAm.setImageResource(R.drawable.dangghiam);
        startTimer();
        speechRecognizer.startListening(speechIntent);
    }

    private void stopRecording() {
        isRecording = false;
        btnGhiAm.setImageResource(R.drawable.nutghiam);

        speechRecognizer.stopListening();
        stopTimer();
        resetTimer();

        // Lấy text Tiếng Việt
        String vietnameseText = txtKetQuaNoi.getText().toString();

        if (!vietnameseText.isEmpty()) {
            // 1. Lưu file gốc Tiếng Việt (để người dùng đọc lại sau này)
            saveTextToFile(vietnameseText);

            // 2. Dịch sang Tiếng Anh rồi mới đoán bệnh
            translateAndPredict(vietnameseText);
        }
    }
    private void translateAndPredict(String vietText) {
        if (vietToEngTranslator == null) return;

        // Bắt đầu dịch
        vietToEngTranslator.translate(vietText)
                .addOnSuccessListener(englishText -> {
                    // Dịch thành công! englishText là văn bản tiếng Anh

                    // --- GỌI AI Ở ĐÂY ---
                    // Lúc này classifier sẽ nhận input tiếng Anh, đúng với word_dict của bạn
                    String ketqua = classifier.predict(englishText);

                    // Hiển thị kết quả
                    String hienThi = "Tiếng Việt: " + vietText +
                            "\n\nTiếng Anh (AI đọc): " + englishText +
                            "\n\n=> AI Chẩn đoán: " + ketqua;

                    txtKetQuaNoi.setText(hienThi);
                    Toast.makeText(this, "Kết quả: " + ketqua, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi dịch thuật", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    // ================= SAVE TEXT =================

    private void saveTextToFile(String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                Toast.makeText(this, "Không có nội dung để lưu", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(new Date());

            File dir = new File(getExternalFilesDir(null), "speech_text");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "speech_" + time + ".txt");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes("UTF-8"));
            fos.close();

            Toast.makeText(
                    this,
                    "Đã lưu file:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi lưu file text", Toast.LENGTH_SHORT).show();
        }
    }

    // ================= TIMER =================

    private void startTimer() {
        startTime = System.currentTimeMillis();
        handler.post(timerRunnable);
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
    }

    private void resetTimer() {
        stopTimer();
        txtThoiGian.setText("00:00");
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long seconds = (System.currentTimeMillis() - startTime) / 1000;
            txtThoiGian.setText(
                    String.format(Locale.getDefault(), "%02d:%02d",
                            seconds / 60, seconds % 60)
            );
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
