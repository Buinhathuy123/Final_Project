package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.final_project.R;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.repository.QuestionRepository;
import com.example.final_project.data.repository.SpeechRepository;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnRecord;
    private LinearLayout btnCauTiepTheo;

    private List<Question> questions;
    private int currentIndex = 0;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String audioFilePath;

    private Handler handler = new Handler();
    private long startTime;

    private static final int REQUEST_RECORD_AUDIO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        initViews();
        checkPermission();
        loadQuestions();

        btnRecord.setOnClickListener(v -> toggleRecording());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);
        btnRecord = findViewById(R.id.btnbatdaughiam1);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    private void toggleRecording() {
        if (!isRecording) startRecording();
        else stopRecording();
    }

    private void startRecording() {
        try {
            isRecording = true;
            startTimer();
            btnRecord.setImageResource(R.drawable.dangghiam);

            txtKetQuaNoi.setText("");

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());

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

            Toast.makeText(this,
                    "🎙 Đang ghi âm...",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Lỗi ghi âm",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            isRecording = false;
            stopTimer();
            btnRecord.setImageResource(R.drawable.nutghiam);

            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            Toast.makeText(this,
                    "📤 Đang chuyển giọng nói...",
                    Toast.LENGTH_SHORT).show();

            new SpeechRepository().transcribeAudio(
                    new File(audioFilePath),
                    new SpeechRepository.SpeechCallback() {

                        @Override
                        public void onSuccess(String text) {
                            runOnUiThread(() -> {
                                txtKetQuaNoi.setText(text);
                                saveTextToFile(text);
                            });
                        }

                        @Override
                        public void onFail(String error) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            HienCauHoiGhiAmActivity.this,
                                            error,
                                            Toast.LENGTH_SHORT).show());
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Lỗi khi dừng ghi âm",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTextToFile(String text) {
        try {
            File dir = new File(getExternalFilesDir(null), "text");
            if (!dir.exists()) dir.mkdirs();

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());

            File file = new File(dir, "answer_" + time + ".txt");

            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadQuestions() {
        new QuestionRepository().loadRandomQuestions(
                new QuestionRepository.QuestionCallback() {

                    @Override
                    public void onSuccess(List<Question> randomQuestions) {
                        questions = randomQuestions;
                        currentIndex = 0;

                        if (questions != null && !questions.isEmpty()) {
                            txtCauHoi.setText(
                                    questions.get(currentIndex).getText());
                        }
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(
                                HienCauHoiGhiAmActivity.this,
                                "Không tải được câu hỏi",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void nextQuestion() {
        if (isRecording) {
            Toast.makeText(this,
                    "Hãy dừng ghi âm trước",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (questions == null) return;

        currentIndex++;
        if (currentIndex < questions.size()) {
            txtCauHoi.setText(
                    questions.get(currentIndex).getText());
            txtKetQuaNoi.setText("");
        } else {
            Toast.makeText(this,
                    "Đã hết câu hỏi",
                    Toast.LENGTH_SHORT).show();
        }
    }

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
            long sec =
                    (System.currentTimeMillis() - startTime) / 1000;

            txtThoiGian.setText(
                    String.format(Locale.getDefault(),
                            "%02d:%02d",
                            sec / 60,
                            sec % 60));

            handler.postDelayed(this, 1000);
        }
    };
}