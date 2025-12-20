package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    private static final String TAG = "GHI_AM";

    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnBatDauGhiAm;
    private LinearLayout btnNgheLai, btnCauTiepTheo;

    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean isRecording = false;
    private String audioPath = "";

    private Handler handler = new Handler();
    private long startTime;

    // ====== CÂU HỎI ======
    private List<Question> questions;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1
        );

        initViews();
        loadQuestions();
        setupActions();
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);

        btnBatDauGhiAm = findViewById(R.id.btnbatdaughiam);
        btnNgheLai = findViewById(R.id.btnnghelaiketqua);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);

        txtKetQuaNoi.setVisibility(View.GONE);
        btnNgheLai.setVisibility(View.GONE);
    }

    private void setupActions() {
        btnBatDauGhiAm.setOnClickListener(v -> {
            if (!isRecording) startRecord();
            else stopRecord();
        });

        btnNgheLai.setOnClickListener(v -> playAudio());

        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    // ================= GHI ÂM =================

    private void startRecord() {
        try {
            File folder = new File(getExternalFilesDir(null), "Recordings");
            if (!folder.exists()) folder.mkdirs();

            audioPath = new File(
                    folder,
                    "record_" + new SimpleDateFormat(
                            "yyyyMMdd_HHmmss", Locale.getDefault()
                    ).format(new Date()) + ".m4a"
            ).getAbsolutePath();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();

            isRecording = true;
            btnBatDauGhiAm.setImageResource(R.drawable.dangghiam);

            startTime = System.currentTimeMillis();
            handler.post(timer);

            Toast.makeText(this, "Bắt đầu ghi âm", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi ghi âm", e);
        }
    }

    private void stopRecord() {
        try {
            recorder.stop();
            recorder.release();
        } catch (Exception ignored) {}

        recorder = null;
        isRecording = false;

        handler.removeCallbacks(timer);
        txtThoiGian.setText("00:00");

        btnBatDauGhiAm.setImageResource(R.drawable.nutghiam);
        btnNgheLai.setVisibility(View.VISIBLE);

        txtKetQuaNoi.setVisibility(View.VISIBLE);
        txtKetQuaNoi.setText("File ghi âm đã lưu tại:\n" + audioPath);

        Toast.makeText(this, "Đã dừng ghi âm", Toast.LENGTH_SHORT).show();
    }

    // ================= PHÁT AUDIO =================

    private void playAudio() {
        try {
            if (player != null) player.release();
            player = new MediaPlayer();
            player.setDataSource(audioPath);
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi phát audio", e);
        }
    }

    // ================= TIMER =================

    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            long sec = (System.currentTimeMillis() - startTime) / 1000;
            txtThoiGian.setText(String.format("%02d:%02d", sec / 60, sec % 60));
            handler.postDelayed(this, 500);
        }
    };

    // ================= CÂU HỎI =================

    private void loadQuestions() {
        new QuestionRepository().loadRandomQuestions(new QuestionRepository.QuestionCallback() {
            @Override
            public void onSuccess(List<Question> list) {
                questions = list;
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

        txtCauHoi.setText(
                "Câu " + (currentIndex + 1) + ": " +
                        questions.get(currentIndex).getText()
        );

        if (currentIndex == 8) {
            btnCauTiepTheo.setVisibility(View.GONE);
        } else {
            btnCauTiepTheo.setVisibility(View.VISIBLE);
        }
    }

    private void nextQuestion() {
        if (currentIndex < 8) {
            currentIndex++;
            showQuestion();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) player.release();
        if (recorder != null) recorder.release();
    }
}
