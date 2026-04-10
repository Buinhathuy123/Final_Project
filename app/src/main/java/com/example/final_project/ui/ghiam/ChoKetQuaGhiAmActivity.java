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

import java.io.File;

public class ChoKetQuaGhiAmActivity extends AppCompatActivity {

    private static final int SAMPLE_RATE = 16000;

    private int scoreText;
    private String resultText;
    private TextView txtTrangThai;

    private Handler loadingHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choketqua_ghiam);

        txtTrangThai = findViewById(R.id.txtTrangThai);

        Intent intent = getIntent();

        scoreText = intent.getIntExtra("score_text", 0);
        resultText = intent.getStringExtra("result_text");

        String pcmPath = intent.getStringExtra("pcmPath");
        long duration = intent.getLongExtra("duration", 0);

        if (pcmPath == null || !new File(pcmPath).exists()) {
            Toast.makeText(this, "File ghi âm lỗi", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        txtTrangThai.setText(
                "Thời lượng ghi âm: " + (duration / 1500) + " giây\n"
        );

        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                float[] raw = PCMUtilActivity.readPCM16(pcmPath);

                // ================= 1. TRIM ĐẦU / CUỐI =================
                float[] processedAudio = trimSilenceEdges(raw, 0.02);

                // fallback nếu trim rỗng
                if (processedAudio.length == 0) {
                    processedAudio = raw;
                }



                // ================= 3. CHECK IM LẶNG LIÊN TỤC 5s =================
                if (hasLongSilence(processedAudio)) {

                    isLoading = false;

                    runOnUiThread(() -> {
                        txtTrangThai.setText("Phát hiện im lặng quá lâu ");

                        new Handler().postDelayed(() -> {
                            Toast.makeText(
                                    this,
                                    "Vui lòng nói liên tục",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(new Intent(
                                    this,
                                    TrangTiepTheoGhiAmActivity.class
                            ));
                            finish();

                        }, 3000);
                    });

                    return;
                }

                // ================= INFER =================
                VoiceModelActivity inferencer =
                        VoiceModelActivity.getInstance(this);

                float[] logits = inferencer.infer(processedAudio);

                long endTime = System.currentTimeMillis();
                long inferTime = endTime - startTime;

                Log.d("DEBUG", "Logits = " + logits[0] + ", " + logits[1]);
                Log.d("DEBUG", "Inference time = " + inferTime + " ms");

                int label = logits[1] > logits[0] ? 1 : 0;

                isLoading = false;

                runOnUiThread(() -> {

                    txtTrangThai.setText(
                            "Xử lý xong trong " + inferTime + " ms"
                    );

                    new Handler().postDelayed(() -> {
                        Intent i = new Intent(
                                this,
                                KetQuaGhiAmActivity.class
                        );
                        i.putExtra("label", label);
                        i.putExtra("score0", logits[0]);
                        i.putExtra("score1", logits[1]);
                        i.putExtra("inferTime", inferTime);

                        // TEXT
                        i.putExtra("final_score", scoreText);
                        i.putExtra("result_text", resultText);

                        startActivity(i);
                        finish();
                    }, 1000);

                });

            } catch (Exception e) {
                Log.e("INFER_ERROR", Log.getStackTraceString(e));
                runOnUiThread(() ->
                        txtTrangThai.setText("Lỗi xử lý giọng nói!")
                );
            }
        }).start();
    }

    // ================= TRIM SILENCE =================
    private float[] trimSilenceEdges(float[] audio, double threshold) {
        int start = 0, end = audio.length - 1;

        for (int i = 0; i < audio.length; i++) {
            if (Math.abs(audio[i]) > threshold) {
                start = i;
                break;
            }
        }

        for (int i = audio.length - 1; i >= 0; i--) {
            if (Math.abs(audio[i]) > threshold) {
                end = i;
                break;
            }
        }

        if (end < start) return new float[0];

        float[] trimmed = new float[end - start + 1];
        System.arraycopy(audio, start, trimmed, 0, trimmed.length);
        return trimmed;
    }

    // ================= DETECT SILENCE 5s =================
    private boolean hasLongSilence(float[] audio) {

        int frameSize = SAMPLE_RATE / 2; // 0.5s
        int silentFrames = 0;
        int maxSilentFrames = 10; // 5s

        for (int i = 0; i + frameSize < audio.length; i += frameSize) {

            double energy = 0;

            for (int j = i; j < i + frameSize; j++) {
                energy += audio[j] * audio[j];
            }

            energy /= frameSize;

            if (energy < 1e-4) {
                silentFrames++;
            } else {
                silentFrames = 0;
            }

            if (silentFrames >= maxSilentFrames) {
                return true;
            }
        }

        return false;
    }
}