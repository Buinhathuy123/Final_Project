package com.example.final_project.ui.ghiam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.util.DataManager; // ✅ Thêm import này

import java.io.File;

public class ChoKetQuaGhiAmActivity extends AppCompatActivity {

    private static final int SAMPLE_RATE = 16000;
    private TextView txtTrangThai;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choketqua_ghiam);

        txtTrangThai = findViewById(R.id.txtTrangThai);

        Intent intent = getIntent();

        String pcmPath = intent.getStringExtra("pcmPath");
        long duration = intent.getLongExtra("duration", 0);

        if (pcmPath == null || !new File(pcmPath).exists()) {
            Toast.makeText(this, "File ghi âm lỗi", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        txtTrangThai.setText("Thời lượng ghi âm: " + (duration / 1000) + " giây\n");

        new Thread(() -> processAudio(pcmPath)).start();
    }

    private void processAudio(String pcmPath) {
        try {
            float[] raw = PCMUtilActivity.readPCM16(pcmPath);

            float[] processedAudio = trimSilenceEdges(raw, 0.02);
            if (processedAudio.length == 0) processedAudio = raw;

            if (hasLongSilence(processedAudio)) {
                runOnUiThread(() -> {
                    txtTrangThai.setText("Phát hiện im lặng quá lâu");
                    handler.postDelayed(() -> {
                        Toast.makeText(this, "Vui lòng nói liên tục", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, TrangGhiAmActivity.class));
                        finish();
                    }, 2000);
                });
                return;
            }

            VoiceModelActivity model = VoiceModelActivity.getInstance(this);
            float[] logits = model.infer(processedAudio);

            // Xác định nhãn trầm cảm từ Voice
            int label = logits[1] > logits[0] ? 1 : 0;

            DataManager.saveVoiceResult(this, label);

            Log.d("FLOW_DEBUG", "Đã lưu kết quả Voice: " + label);

            runOnUiThread(() -> {

                handler.postDelayed(() -> {
                    Intent i = new Intent(this, KetQuaGhiAmActivity.class);
                    i.putExtra("label_voice", label);
                    i.putExtra("score0", logits[0]);
                    i.putExtra("score1", logits[1]);

                    startActivity(i);
                    finish();
                }, 1000);
            });

        } catch (Exception e) {
            Log.e("INFER_ERROR", Log.getStackTraceString(e));
            runOnUiThread(() -> txtTrangThai.setText("Lỗi xử lý giọng nói!"));
        }
    }

    private float[] trimSilenceEdges(float[] audio, double threshold) {
        int start = 0, end = audio.length - 1;
        for (int i = 0; i < audio.length; i++) {
            if (Math.abs(audio[i]) > threshold) { start = i; break; }
        }
        for (int i = audio.length - 1; i >= 0; i--) {
            if (Math.abs(audio[i]) > threshold) { end = i; break; }
        }
        if (end < start) return new float[0];
        float[] trimmed = new float[end - start + 1];
        System.arraycopy(audio, start, trimmed, 0, trimmed.length);
        return trimmed;
    }

    private boolean hasLongSilence(float[] audio) {
        int frameSize = SAMPLE_RATE / 2;
        int silentFrames = 0;
        for (int i = 0; i + frameSize < audio.length; i += frameSize) {
            double energy = 0;
            for (int j = i; j < i + frameSize; j++) { energy += audio[j] * audio[j]; }
            energy /= frameSize;
            if (energy < 1e-4) silentFrames++;
            else silentFrames = 0;
            if (silentFrames >= 10) return true;
        }
        return false;
    }
}