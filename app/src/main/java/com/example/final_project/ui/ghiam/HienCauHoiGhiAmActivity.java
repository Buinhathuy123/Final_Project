package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.final_project.R;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.model.SpeechResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;
import com.example.final_project.data.repository.QuestionRepository;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HienCauHoiGhiAmActivity extends AppCompatActivity {

    // UI
    private TextView txtCauHoi, txtKetQuaNoi, txtThoiGian;
    private ImageView btnGhiAm;
    private LinearLayout btnCauTiepTheo;

    // Question
    private List<Question> questions;
    private int currentIndex = 0;

    // Recording
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;

    // Timer
    private Handler handler = new Handler();
    private long startTime;

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

        initViews();
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

    // ================= RECORD =================

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        try {
            isRecording = true;
            btnGhiAm.setImageResource(R.drawable.dangghiam);
            startTimer();

            String time = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(new Date());

            File dir = new File(getExternalFilesDir(null), "audio");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "record_" + time + ".m4a");
            audioFilePath = file.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            isRecording = false;
            btnGhiAm.setImageResource(R.drawable.nutghiam);
            stopTimer();
            resetTimer();

            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            // 🔥 GỬI AUDIO LÊN SERVER
            uploadAudioToServer(audioFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UPLOAD AUDIO =================

    private void uploadAudioToServer(String path) {
        File file = new File(path);

        RequestBody requestBody =
                RequestBody.create(file, MediaType.parse("audio/*"));

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("audio", file.getName(), requestBody);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.uploadAudio(body).enqueue(new Callback<SpeechResponse>() {
            @Override
            public void onResponse(Call<SpeechResponse> call, Response<SpeechResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isOk()) {

                    txtKetQuaNoi.setText(response.body().getText());

                } else {
                    Toast.makeText(
                            HienCauHoiGhiAmActivity.this,
                            "Không nhận được text",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<SpeechResponse> call, Throwable t) {
                Toast.makeText(
                        HienCauHoiGhiAmActivity.this,
                        "Lỗi kết nối server",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
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
}
