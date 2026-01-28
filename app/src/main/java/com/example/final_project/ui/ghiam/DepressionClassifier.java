package com.example.final_project.ui.ghiam;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DepressionClassifier {
    private Interpreter interpreter;
    private Map<String, Integer> wordDict = new HashMap<>();
    private int MAX_LEN = 100;

    public DepressionClassifier(Context context) {
        try {
            // 1. Load Model từ assets
            interpreter = new Interpreter(loadModelFile(context, "model_depression.tflite"));
            // 2. Load Từ điển từ assets
            loadWordDict(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String predict(String text) {
        if (interpreter == null) return "Lỗi Model";

        // --- BƯỚC 1: KIỂM TRA TỪ KHÓA NGUY HIỂM (Hardcode rule) ---
        // Nếu phát hiện từ khóa tiêu cực mạnh -> Gán luôn mức nặng nhất
        String textLower = text.toLowerCase();
        if (textLower.contains("kill") ||
                textLower.contains("suicide") ||
                textLower.contains("die") ||
                textLower.contains("death") ||
                textLower.contains("hurt myself")) {

            return "Trầm cảm Nặng (Cảnh báo nguy hiểm)";
        }
        // -----------------------------------------------------------

        // --- BƯỚC 2: NẾU KHÔNG CÓ TỪ KHÓA, ĐỂ AI LO ---
        float[][] input = preprocessText(text);
        float[][] output = new float[1][5];

        interpreter.run(input, output);

        // Log để kiểm tra như cũ
        float[] probs = output[0];
        System.out.println("DEBUG_PROBS: " + Arrays.toString(probs));

        return getLabel(output[0]);
    }

    private float[][] preprocessText(String text) {
        float[][] sequence = new float[1][MAX_LEN];


        String cleanText = text.toLowerCase().replaceAll("[^a-z ]", "");

        String[] words = cleanText.split("\\s+");

        System.out.println("DEBUG_AI_INPUT: " + cleanText);

        for (int i = 0; i < Math.min(words.length, MAX_LEN); i++) {
            if (wordDict.containsKey(words[i])) {
                sequence[0][i] = wordDict.get(words[i]);
                System.out.println("DEBUG_TOKEN: " + words[i] + " -> " + wordDict.get(words[i]));
            } else {
                sequence[0][i] = 0;
                System.out.println("DEBUG_UNKNOWN: " + words[i]);
            }
        }
        return sequence;
    }

    private String getLabel(float[] probabilities) {
        int maxIndex = 0;
        float maxProb = -1;

        // Tìm xác suất lớn nhất trong 5 mức
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        // Mapping 5 mức độ như bạn yêu cầu
        //String nhan = "";
        switch (maxIndex) {
            case 4: return "Bình thường (Không bị trầm cảm)"; // <--- Index 4 là Happy
            case 3: return "Trầm cảm Nhẹ";
            case 2: return "Trầm cảm Vừa";
            case 1: return "Trầm cảm Nặng vừa";
            case 0: return "Trầm cảm Nặng";                    // <--- Index 0 là Severe
            default: return "Không xác định";
        }


    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadWordDict(Context context) {
        try {
            InputStream is = context.getAssets().open("word_dict.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(jsonStr);
            Iterator<String> keys = jsonObject.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                int value = jsonObject.getInt(key);
                wordDict.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}