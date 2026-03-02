package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnVoiceToFile;
    private LinearLayout btnCauTiepTheo;

    // ================= QUESTIONS =================
    private List<Question> questions;
    private int currentIndex = 0;

    // ================= TIMER =================
    private Handler handler = new Handler();
    private long startTime;

    // ================= AUDIO =================
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File pcmFile;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MIN_RECORD_TIME = 10; // giây
    private static final int MAX_RECORD_TIME = 30; // giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        ActivityCompat.requestPermissions(
                this,
                new String[]{ Manifest.permission.RECORD_AUDIO },
                1
        );

        initViews();
        loadQuestions();

        btnVoiceToFile.setOnClickListener(v -> toggleRecording());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    // ================= INIT =================

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);

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
                        questions = randomQuestions;
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
        txtCauHoi.setText(questions.get(currentIndex).getText());
        txtKetQuaNoi.setText("");
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

    // ================= RECORD =================

    private void toggleRecording() {
        if (!isRecording) {
            startPCMRecording();
        } else {
            stopPCMRecording();
        }
    }

    private void startPCMRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL, ENCODING
        );

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) return;

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL,
                ENCODING,
                bufferSize
        );

        File dir = new File(getExternalFilesDir(null), "pcm");
        if (!dir.exists()) dir.mkdirs();

        String time = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(new Date());

        pcmFile = new File(dir, "answer_" + time + ".pcm");

        audioRecord.startRecording();
        isRecording = true;
        startTime = System.currentTimeMillis();
        startTimer();

        new Thread(() -> writePCM(bufferSize)).start();
        Toast.makeText(this, "🎙️ Đang ghi âm...", Toast.LENGTH_SHORT).show();
    }

    private void writePCM(int bufferSize) {
        byte[] buffer = new byte[bufferSize];

        try (FileOutputStream fos = new FileOutputStream(pcmFile)) {
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) fos.write(buffer, 0, read);

                long sec = (System.currentTimeMillis() - startTime) / 1000;
                if (sec >= MAX_RECORD_TIME) {
                    runOnUiThread(this::stopPCMRecording);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPCMRecording() {

        // 1️⃣ Kiểm tra có đang ghi âm không
        if (!isRecording) {
            Log.d("RECORD", "stopPCMRecording: not recording");
            return;
        }

        // 2️⃣ Dừng ghi âm
        isRecording = false;

        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.e("RECORD", "Error stopping AudioRecord", e);
        }

        // 3️⃣ Tính thời gian ghi âm
        long endTime = System.currentTimeMillis();
        long recordDuration = endTime - startTime;
        // ms

        Log.d("RECORD", "Record duration = " + recordDuration + " ms");

        // 4️⃣ Kiểm tra thời gian tối thiểu 30 giây
        if (recordDuration < 10_000) {
            Toast.makeText(
                    this,
                    "Ghi âm tối thiểu 10 giây",
                    Toast.LENGTH_SHORT
            ).show();

            // ❌ KHÔNG start activity
            // ❌ KHÔNG finish
            return;
        }

        // 5️⃣ Kiểm tra file PCM có tồn tại không
        if (pcmFile == null || !pcmFile.exists()) {
            Toast.makeText(
                    this,
                    "Lỗi file ghi âm",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 6️⃣ Chuyển sang màn hình kết quả
        Intent intent = new Intent(
                HienCauHoiGhiAmActivity.this,
                ChoKetQuaGhiAmActivity.class
        );
        intent.putExtra("pcmPath", pcmFile.getAbsolutePath());
        intent.putExtra("duration", recordDuration);

        Log.d("RECORD", "Start ChoKetQuaGhiAmActivity");
        startActivity(intent);

        // 7️⃣ Đóng activity ghi âm SAU KHI startActivity
        finish();
    }


    // ================= TIMER =================

    private void startTimer() {
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
}
