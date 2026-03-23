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
    private static final int WINDOW_SEC = 8;
    private static final int WINDOW_SIZE = SAMPLE_RATE * WINDOW_SEC;

    private TextView txtTrangThai;
    private TextView txtLoadingDots;

    private Handler loadingHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choketqua_ghiam);

        txtTrangThai = findViewById(R.id.txtTrangThai);
        txtLoadingDots = findViewById(R.id.txtLoadingDots);

        Intent intent = getIntent();
        String pcmPath = intent.getStringExtra("pcmPath");
        long duration = intent.getLongExtra("duration", 0);

        if (pcmPath == null || !new File(pcmPath).exists()) {
            Toast.makeText(this, "File ghi âm lỗi", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        txtTrangThai.setText(
                "Thời lượng ghi âm: " + (duration / 1000) + " giây\n"
        );

        startLoadingAnimation();

        new Thread(() -> {
            try {

                long startTime = System.currentTimeMillis();

                float[] raw = PCMUtilActivity.readPCM16(pcmPath);
                float[] best8s = selectBestEnergyWindow(raw);

                Log.d("DEBUG", "Audio length = " + best8s.length);

                VoiceModelActivity inferencer =
                        VoiceModelActivity.getInstance(this);

                float[] logits = inferencer.infer(best8s);

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

    // ================= LOADING DOTS =================
    private void startLoadingAnimation() {

        loadingHandler.post(new Runnable() {

            int dotCount = 0;

            @Override
            public void run() {

                if (!isLoading) return;

                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < dotCount; i++) {
                    dots.append("● ");
                }

                txtLoadingDots.setText(dots.toString());

                dotCount++;
                if (dotCount > 3) dotCount = 0;

                loadingHandler.postDelayed(this, 400);
            }
        });
    }

    // ================= ENERGY WINDOW =================
    private float[] selectBestEnergyWindow(float[] audio) {

        if (audio.length <= WINDOW_SIZE) {
            float[] out = new float[WINDOW_SIZE];
            System.arraycopy(audio, 0, out, 0, audio.length);
            return out;
        }

        int bestStart = 0;
        double bestEnergy = -1;

        for (int i = 0; i + WINDOW_SIZE <= audio.length; i += SAMPLE_RATE) {
            double energy = 0;
            for (int j = i; j < i + WINDOW_SIZE; j++) {
                energy += audio[j] * audio[j];
            }
            if (energy > bestEnergy) {
                bestEnergy = energy;
                bestStart = i;
            }
        }

        float[] best = new float[WINDOW_SIZE];
        System.arraycopy(audio, bestStart, best, 0, WINDOW_SIZE);
        return best;
    }
}
