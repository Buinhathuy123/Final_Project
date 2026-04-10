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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrangTiepTheoGhiAmActivity extends AppCompatActivity {

    // UI
    private TextView txtCauHoi, txtThoiGian;
    private ImageView btnVoice;
    private TextView txtKetQuaNoi;
    // DATA từ trang 1
    private int scoreText;
    private String resultText;

    // AUDIO
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File pcmFile;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MIN_RECORD_TIME = 15; // giây
    private static final int MAX_RECORD_TIME = 30; // giây

    // TIMER
    private Handler handler = new Handler();
    private long startTime;

    private final String question2 = "Gần đây bạn có gặp khó khăn trong việc đi vào giấc ngủ, ngủ không sâu giấc hoặc ngủ quá nhiều không?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam_2);

        // 🔥 Nhận dữ liệu từ trang 1
        scoreText = getIntent().getIntExtra("score_text", 0);
        resultText = getIntent().getStringExtra("result_text");

        initViews();

        txtCauHoi.setText(question2);

        btnVoice.setOnClickListener(v -> toggleRecording());

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1
        );
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtThoiGian = findViewById(R.id.textthoigianghiam);
        btnVoice = findViewById(R.id.btnbatdaughiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
    }

    // ================= RECORD =================

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
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

        btnVoice.setImageResource(R.drawable.dangghiam);
        txtKetQuaNoi.setText("🎙️ Đang ghi âm...");

    }

    private void writePCM(int bufferSize) {
        byte[] buffer = new byte[bufferSize];

        try (FileOutputStream fos = new FileOutputStream(pcmFile)) {
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) fos.write(buffer, 0, read);

                long sec = (System.currentTimeMillis() - startTime) / 1500;
                if (sec >= MAX_RECORD_TIME) {
                    runOnUiThread(this::stopRecording);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void stopRecording() {

        // 1️⃣ Check đang ghi không
        if (!isRecording) {
            Log.d("RECORD", "stopRecording: not recording");
            return;
        }

        // 2️⃣ Stop recording
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

        // 3️⃣ Stop timer + UI
        stopTimer();
        btnVoice.setImageResource(R.drawable.nutghiam);
        txtKetQuaNoi.setText("Đã ghi âm xong");

        // 4️⃣ Tính thời gian
        long recordDuration = System.currentTimeMillis() - startTime;
        Log.d("RECORD", "Duration = " + recordDuration + " ms");

        // 5️⃣ Check thời gian tối thiểu
        if (recordDuration < MIN_RECORD_TIME * 1500) {
            Toast.makeText(this, "Ghi âm tối thiểu 15 giây", Toast.LENGTH_SHORT).show();
            return;
        }

        // 6️⃣ Check file PCM
        if (pcmFile == null || !pcmFile.exists()) {
            Toast.makeText(this, "Lỗi file ghi âm", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "✔️ Ghi âm hoàn tất", Toast.LENGTH_SHORT).show();

        // 7️⃣ CHUYỂN SANG CHỜ KẾT QUẢ
        Intent intent = new Intent(this, ChoKetQuaGhiAmActivity.class);

        // TEXT từ trang 1
        intent.putExtra("score_text", scoreText);
        intent.putExtra("result_text", resultText);

        // AUDIO
        intent.putExtra("pcmPath", pcmFile.getAbsolutePath());
        intent.putExtra("duration", recordDuration);

        startActivity(intent);
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
            long sec = (System.currentTimeMillis() - startTime) / 1500;
            txtThoiGian.setText(
                    String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60)
            );
            handler.postDelayed(this, 1500);
        }
    };
}
