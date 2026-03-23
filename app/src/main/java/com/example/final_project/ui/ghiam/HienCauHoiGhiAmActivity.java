package com.example.final_project.ui.ghiam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import com.example.final_project.data.model.Question;
import com.example.final_project.data.repository.SpeechRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    // ================= SPEECH TO TEXT =================
    private SpeechRecognizer speechRecognizer;
    private StringBuilder recognitionResult = new StringBuilder();
    private File txtFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manhinhcho_ghiam);

        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.RECORD_AUDIO },
                1);

        initViews();
        loadQuestions();
        initSpeechRecognizer();

        btnVoiceToFile.setOnClickListener(v -> toggleRecording());
        btnCauTiepTheo.setOnClickListener(v -> nextQuestion());
    }

    private void initViews() {
        txtCauHoi = findViewById(R.id.textcauhoighiam);
        txtKetQuaNoi = findViewById(R.id.txtKetQuaNoi);
        txtThoiGian = findViewById(R.id.textthoigianghiam);

        btnVoiceToFile = findViewById(R.id.btnbatdaughiam2);
        btnCauTiepTheo = findViewById(R.id.btn_cautieptheo);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO);
        }
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
                        Toast.makeText(
                                HienCauHoiGhiAmActivity.this,
                                "Không tải được câu hỏi",
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
                SAMPLE_RATE, CHANNEL, ENCODING);

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            return;

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL,
                ENCODING,
                bufferSize);

        File dir = new File(getExternalFilesDir(null), "pcm");
        if (!dir.exists())
            dir.mkdirs();

        String time = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        pcmFile = new File(dir, "answer_" + time + ".pcm");
        txtFile = new File(dir, "answer_" + time + ".txt");

        audioRecord.startRecording();
        isRecording = true;
        startTime = System.currentTimeMillis();
        startTimer();

        startSpeechToText();

        new Thread(() -> writePCM(bufferSize)).start();
        Toast.makeText(this, "🎙️ Đang ghi âm...", Toast.LENGTH_SHORT).show();
    }

    private void writePCM(int bufferSize) {
        byte[] buffer = new byte[bufferSize];

        try (FileOutputStream fos = new FileOutputStream(pcmFile)) {
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0)
                    fos.write(buffer, 0, read);

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

        stopSpeechToText();

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
                    Toast.LENGTH_SHORT).show();

            // ❌ KHÔNG start activity
            // ❌ KHÔNG finish
            return;
        }

        // 5️⃣ Kiểm tra file PCM có tồn tại không
        if (pcmFile == null || !pcmFile.exists()) {
            Toast.makeText(
                    this,
                    "Lỗi file ghi âm",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 5.5️⃣ Lưu kết quả nhận diện giọng nói (nếu có)
        String finalSpeechText = txtKetQuaNoi.getText().toString();
        if (!finalSpeechText.isEmpty()) {
            saveTextToFile(finalSpeechText);
        }

        // 6️⃣ Chuyển sang màn hình kết quả
        Intent intent = new Intent(
                HienCauHoiGhiAmActivity.this,
                ChoKetQuaGhiAmActivity.class);
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
                            "%02d:%02d", sec / 60, sec % 60));
            handler.postDelayed(this, 1000);
        }
    };

    // ================= SPEECH TO TEXT HELPERS =================

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "Lỗi âm thanh";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "Lỗi kết nối client";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Thiếu quyền ghi âm";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "Lỗi mạng";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "Mạng quá tải";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "Không nhận diện được giọng nói";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "Dịch vụ đang bận";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "Lỗi máy chủ";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "Không nghe thấy tiếng";
                            break;
                        default:
                            message = "Lỗi không xác định: " + error;
                            break;
                    }
                    Log.e("STT", "Error: " + message);
                    runOnUiThread(
                            () -> Toast.makeText(HienCauHoiGhiAmActivity.this, message, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        txtKetQuaNoi.setText(text);
                        saveTextToFile(text);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        txtKetQuaNoi.setText(matches.get(0));
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    private void startSpeechToText() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizer.startListening(intent);
        }
    }

    private void stopSpeechToText() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    private void saveTextToFile(String text) {
        if (txtFile == null)
            return;
        try (FileWriter writer = new FileWriter(txtFile)) {
            writer.write(text);
            Log.d("STT", "Saved text to: " + txtFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("STT", "Error saving text file", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
