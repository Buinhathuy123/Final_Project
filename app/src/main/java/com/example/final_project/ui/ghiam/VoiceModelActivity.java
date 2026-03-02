package com.example.final_project.ui.ghiam;

import android.content.Context;
import android.util.Log;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

public class VoiceModelActivity {

    private static VoiceModelActivity instance;

    private OrtEnvironment env;
    private OrtSession session;

    private VoiceModelActivity(Context context) throws Exception {

        env = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

        session = env.createSession(
                assetFilePath(context,
                        "voiceStressDetector.onnx"),
                options
        );

        Log.d("INFER", "ONNX model loaded");
    }

    public static synchronized VoiceModelActivity getInstance(Context context)
            throws Exception {

        if (instance == null) {
            instance = new VoiceModelActivity(
                    context.getApplicationContext()
            );
        }
        return instance;
    }

    // ================= INFERENCE =================
    public float[] infer(float[] audio) throws Exception {

        long[] shape = new long[]{1, audio.length};

        OnnxTensor inputTensor = OnnxTensor.createTensor(
                env,
                FloatBuffer.wrap(audio),
                shape
        );

        OrtSession.Result result = session.run(
                Collections.singletonMap(
                        "input_values",
                        inputTensor
                )
        );

        float[][] output =
                (float[][]) result.get(0).getValue();

        inputTensor.close();
        result.close();

        return output[0];  // size = 2
    }

    // ================= COPY ASSET =================
    private static String assetFilePath(Context context, String assetName)
            throws Exception {

        File file = new File(context.getFilesDir(), assetName);

        if (file.exists() && file.length() > 0)
            return file.getAbsolutePath();

        InputStream is = context.getAssets().open(assetName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int read;

        while ((read = is.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.close();
        is.close();

        return file.getAbsolutePath();
    }
}
