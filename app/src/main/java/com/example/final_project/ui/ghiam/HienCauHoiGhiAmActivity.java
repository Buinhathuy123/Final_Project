package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.Intent;
import android.media.MediaRecorder;
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
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnVoiceToText, btnVoiceToFile;
    private LinearLayout btnCauTiepTheo;

    // ================= QUESTIONS =================
    private List<Question> questions;
    private int currentIndex = 0;

    // ================= TIMER =================
    private Handler handler = new Handler();
    private long startTime;

    // ================= VOICE TO TEXT =================
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    // ================= VOICE TO FILE =================
    private MediaRecorder mediaRecorder;
    private boolean isRecordingFile = false;
    private String audioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1
        );

        initViews();
        initSpeechRecognizer();
        loadQuestions();

        btnVoiceToText.setOnClickListener(v -> toggleVoiceToText());
        btnVoiceToFile.setOnClickListener(v -> toggleVoiceToFile());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    // ================= INIT =================

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);

        btnVoiceToText = findViewById(R.id.btnbatdaughiam1);
        btnVoiceToFile = findViewById(R.id.btnbatdaughiam2);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);

        txtKetQuaNoi.setText("");
        txtThoiGian.setText("00:00");
    }

    // ================= QUESTIONS =================

    private void loadQuestions() {
        new QuestionRepository().loadRandomQuestions(
                new QuestionRepository.QuestionCallback() {
                    @Override
                    public void onSuccess(List<Question> randomQuestions) {
                        if (randomQuestions == null || randomQuestions.isEmpty()) {
                            txtCauHoi.setText("Không có câu hỏi");
                            return;
                        }
                        questions = randomQuestions;
                        currentIndex = 0;
                        showQuestion();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(
                                HienCauHoiGhiAmActivity.this,
                                "Không tải được câu hỏi",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty()) return;
        txtCauHoi.setText(questions.get(currentIndex).getText());
        txtKetQuaNoi.setText("");
    }

    private void nextQuestion() {
        if (isListening || isRecordingFile) {
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

    // ================= VOICE TO TEXT =================

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "vi-VN"
        );

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                txtKetQuaNoi.setText("❌ Không nhận dạng được");
                stopVoiceToText();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> texts =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (texts != null && !texts.isEmpty()) {
                    String result = texts.get(0);
                    txtKetQuaNoi.setText(result);

                    // ✅ LƯU TEXT RA FILE + THÔNG BÁO ĐƯỜNG DẪN
                    saveTextToFile(result);
                }
                stopVoiceToText();
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void toggleVoiceToText() {
        if (isRecordingFile) {
            Toast.makeText(this, "Đang ghi file audio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isListening) {
            isListening = true;
            startTimer();
            setRecordingUI();
            txtKetQuaNoi.setText("🎤 Đang nghe...");
            speechRecognizer.startListening(speechIntent);
        } else {
            stopVoiceToText();
        }
    }

    private void stopVoiceToText() {
        if (!isListening) return;

        isListening = false;
        stopTimer();
        setIdleUI();
        speechRecognizer.stopListening();
    }

    // ================= VOICE TO FILE =================

    private void toggleVoiceToFile() {
        if (isListening) {
            Toast.makeText(this, "Đang nhận dạng giọng nói", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isRecordingFile) {
            startRecordingFile();
        } else {
            stopRecordingFile();
        }
    }

    private void startRecordingFile() {
        try {
            isRecordingFile = true;
            startTimer();

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(new Date());

            File dir = new File(getExternalFilesDir(null), "audio");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "answer_" + time + ".m4a");
            audioFilePath = file.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(this, "🎙️ Bắt đầu ghi file", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingFile() {
        try {
            isRecordingFile = false;
            stopTimer();

            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            Toast.makeText(
                    this,
                    "💾 Đã lưu audio:\n" + audioFilePath,
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SAVE TEXT FILE =================

    private void saveTextToFile(String text) {
        try {
            File dir = new File(getExternalFilesDir(null), "text");
            if (!dir.exists()) dir.mkdirs();

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(new Date());

            File file = new File(dir, "answer_" + time + ".txt");
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();

            // ✅ THÔNG BÁO ĐƯỜNG DẪN FILE TXT
            Toast.makeText(
                    this,
                    "📄 Đã lưu text:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TIMER =================

    private void startTimer() {
        startTime = System.currentTimeMillis();
        handler.post(timerRunnable);
    }

    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
        txtThoiGian.setText("00:00");
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long sec = (System.currentTimeMillis() - startTime) / 1000;
            txtThoiGian.setText(
                    String.format(Locale.getDefault(),
                            "%02d:%02d", sec / 60, sec % 60)
            );
            handler.postDelayed(this, 1000);
        }
    };

    // ================= UI EFFECT =================

    private void setRecordingUI() {
        btnVoiceToText.setImageResource(R.drawable.dangghiam);
        btnVoiceToText.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(250)
                .start();
    }

    private void setIdleUI() {
        btnVoiceToText.setImageResource(R.drawable.nutghiam);
        btnVoiceToText.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .start();
    }
}
