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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private long totalDuration = 0; // Lưu tổng thời gian của tất cả các câu

    // ================= AUDIO =================
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File sessionPcmFile; // File lưu toàn bộ ghi âm để đưa vào Model AI dự đoán trầm cảm

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MAX_RECORD_TIME = 30; // giây tối đa cho mỗi câu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        // Xin quyền ghi âm
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.RECORD_AUDIO },
                1);

        initViews();
        initSessionFiles();
        loadQuestions();

        btnVoiceToFile.setOnClickListener(v -> toggleRecording());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);
        btnVoiceToFile = findViewById(R.id.btnbatdaughiam);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);
    }

    private void initSessionFiles() {
        File dir = new File(getExternalFilesDir(null), "pcm");
        if (!dir.exists()) dir.mkdirs();

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Khởi tạo file dùng chung cho toàn bộ quá trình trả lời (AI trầm cảm cần file này)
        sessionPcmFile = new File(dir, "session_" + time + ".pcm");
    }

    // ================= LOCAL QUESTIONS =================
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
                        // Thêm Log và in thẳng lỗi ra màn hình để biết tại sao
                        Log.e("DATA_ERROR", "Lỗi tải câu hỏi: " + error);
                        Toast.makeText(
                                HienCauHoiGhiAmActivity.this,
                                "Lỗi: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showQuestion() {
        txtCauHoi.setText(questions.get(currentIndex).getText());
        txtKetQuaNoi.setText("Hãy bấm nút tròn để bắt đầu trả lời"); // Trạng thái mặc định
    }

    private void nextQuestion() {
        // --- BƯỚC BẢO VỆ CHỐNG SẬP APP ---
        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "Đang tải câu hỏi hoặc dữ liệu bị lỗi, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return; // Chặn không cho chạy tiếp các lệnh bên dưới
        }

        if (isRecording) {
            Toast.makeText(this, "Hãy dừng ghi âm trước khi qua câu tiếp theo", Toast.LENGTH_SHORT).show();
            return;
        }

        currentIndex++;

        // Kiểm tra nếu đã trả lời xong tất cả các câu
        if (currentIndex >= questions.size()) {

            // Yêu cầu thời lượng tối thiểu tổng cộng 10s cho model
            if (totalDuration < 10_000) {
                Toast.makeText(this, "Tổng thời gian ghi âm các câu phải trên 10 giây", Toast.LENGTH_SHORT).show();
                currentIndex--; // Lùi lại để người dùng có thể ghi âm thêm
                return;
            }

            // Ghi âm xong hết -> Chuyển sang màn hình chờ kết quả của AI trầm cảm
            Intent intent = new Intent(HienCauHoiGhiAmActivity.this, ChoKetQuaGhiAmActivity.class);
            intent.putExtra("pcmPath", sessionPcmFile.getAbsolutePath());
            intent.putExtra("duration", totalDuration);
            startActivity(intent);
            finish();
            return;
        }

        // Hiện câu hỏi tiếp theo
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
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Chưa cấp quyền ghi âm!", Toast.LENGTH_SHORT).show();
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);
        audioRecord.startRecording();
        isRecording = true;
        startTime = System.currentTimeMillis();
        startTimer();

        // Cập nhật trạng thái
        txtKetQuaNoi.setText("Đang ghi âm... ");

        new Thread(() -> writePCM(bufferSize)).start();
    }

    private void writePCM(int bufferSize) {
        byte[] buffer = new byte[bufferSize];

        // Mở file ở chế độ append (true) để nối âm thanh các câu hỏi lại với nhau
        try (FileOutputStream fosSession = new FileOutputStream(sessionPcmFile, true)) {
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    fosSession.write(buffer, 0, read);
                }

                long sec = (System.currentTimeMillis() - startTime) / 1000;
                if (sec >= MAX_RECORD_TIME) {
                    isRecording = false; // Tự động dừng nếu nói quá lâu
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // KHI isRecording = false, VÒNG LẶP KẾT THÚC, FILE ĐÃ ĐƯỢC LƯU VÀ ĐÓNG

        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            Log.e("RECORD", "Lỗi dừng AudioRecord", e);
        }

        long recordDuration = System.currentTimeMillis() - startTime;
        totalDuration += recordDuration; // Cộng dồn thời gian tổng

        runOnUiThread(() -> {
            stopTimer(); // Dừng đồng hồ
            txtKetQuaNoi.setText("Đã ghi âm xong câu này. Bạn có thể bấm Câu tiếp theo.");
        });
    }

    private void stopPCMRecording() {
        if (!isRecording) return;

        // Ra lệnh dừng ghi âm
        isRecording = false;
        txtKetQuaNoi.setText("Đang lưu âm thanh...");
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
                            "%02d:%02d", sec / 60, sec % 60));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}