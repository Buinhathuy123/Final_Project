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

public class TrangGhiAmActivity extends AppCompatActivity {

    private TextView txtCauHoi, txtThoiGian, txtKetQuaNoi;
    private ImageView btnVoice;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File pcmFile;

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MIN_RECORD_TIME = 15;
    private static final int MAX_RECORD_TIME = 30;

    private Handler handler = new Handler();
    private long startTime;

    private final String question2 =
            "Gần đây bạn có gặp khó khăn trong việc đi vào giấc ngủ, ngủ không sâu giấc hoặc ngủ quá nhiều không?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);
        showGuideDialog();

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

    private void toggleRecording() {
        if (!isRecording) startRecording();
        else stopRecording();
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

                if (read > 0) {
                    fos.write(buffer, 0, read);
                }

                long sec = (System.currentTimeMillis() - startTime) / 1000;

                if (sec >= MAX_RECORD_TIME) {
                    runOnUiThread(this::stopRecording);
                }
            }
        } catch (Exception e) {
            Log.e("RECORD_ERROR", "Write PCM lỗi", e);
        }
    }

    private void stopRecording() {

        if (!isRecording) return;

        isRecording = false;

        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.e("RECORD", "Stop lỗi", e);
        }

        stopTimer();
        btnVoice.setImageResource(R.drawable.nutghiam);
        txtKetQuaNoi.setText("Đã ghi âm xong");

        long recordDuration = System.currentTimeMillis() - startTime;

        if (recordDuration < MIN_RECORD_TIME * 1000) {
            Toast.makeText(this, "Vui lòng nói đủ 15 giây", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pcmFile == null || !pcmFile.exists()) {
            Toast.makeText(this, "Lỗi file ghi âm", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChoKetQuaGhiAmActivity.class);

        intent.putExtra("pcmPath", pcmFile.getAbsolutePath());
        intent.putExtra("duration", recordDuration);

        startActivity(intent);
        finish();
    }

    private void startTimer() {
        handler.post(timerRunnable);
    }
    private void showGuideDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_luu_y_ghiam, null);

        builder.setView(view);
        builder.setCancelable(false);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.btnDaHieu).setOnClickListener(v -> dialog.dismiss());
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