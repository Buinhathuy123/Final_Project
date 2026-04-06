package com.example.final_project.text;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteHelper {
    private Interpreter interpreter;
    private TokenizerHelper tokenizer;
    private static final int MAX_LEN = 120;

    // 🔥 ĐẢM BẢO: Thứ tự này phải khớp 100% với lúc bạn train Model
    private final String[] labels = {"normal", "minimal", "mild", "moderate", "severe"};

    public TFLiteHelper(Context context) {
        try {
            interpreter = new Interpreter(loadModelFile(context));
            tokenizer = new TokenizerHelper(context);
            Log.d("TFLITE", "✅ Model & Tokenizer loaded OK");
        } catch (Exception e) {
            Log.e("TFLITE", "❌ Lỗi khởi tạo", e);
            interpreter = null;
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws Exception {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("depression_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    public String predict(String text) {
        if (interpreter == null || tokenizer == null) return "Model chưa load!";

        try {
            // 1. Tiền xử lý chuẩn cho tiếng Việt
            String cleanText = text.toLowerCase()
                    .replaceAll("[.,/#!$%^&*;:{}=\\-_`~()]", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            if (cleanText.isEmpty()) return "normal";

            // --- 2. BỘ LỌC HYBRID (CHẶN TỪ KHÓA ĐỂ TĂNG ĐỘ CHÍNH XÁC) ---

            // A. Ưu tiên Tích cực (Bình thường)
            if (cleanText.contains("vui vẻ") || cleanText.contains("rất ổn") ||
                    cleanText.contains("yêu đời") || cleanText.contains("hạnh phúc") ||
                    cleanText.contains("ngủ rất ngon")) {
                return "normal";
            }

            // B. Chặn Tiêu cực cực đoan (Nghiêm trọng)
            if (cleanText.contains("muốn chết") || cleanText.contains("tự tử") ||
                    cleanText.contains("bế tắc") || cleanText.contains("tuyệt vọng")) {
                return "severe";
            }

            // C. Nhận diện dấu hiệu trung gian (Nếu AI hay đoán sai về 'normal')
            if (cleanText.contains("mất ngủ") || cleanText.contains("chán nản") || cleanText.contains("mệt mỏi")) {
                String aiResult = runInference(cleanText);
                // Nếu có từ khóa buồn mà AI vẫn bảo bình thường -> ép lên mức thấp nhất của trầm cảm
                return aiResult.equals("normal") ? "minimal" : aiResult;
            }

            // --- 3. CHẠY AI DỰ ĐOÁN ---
            return runInference(cleanText);

        } catch (Exception e) {
            Log.e("AI_ERROR", "Dự đoán thất bại", e);
            return "normal";
        }
    }

    private String runInference(String cleanText) {
        // Chuyển text thành Sequence
        int[] sequence = tokenizer.textToSequence(cleanText, MAX_LEN);

        // Chuẩn bị đầu vào Float32
        float[][] input = new float[1][MAX_LEN];
        for (int i = 0; i < MAX_LEN; i++) {
            input[0][i] = (float) sequence[i];
        }

        // Chạy model trả về 5 nhãn xác suất
        float[][] output = new float[1][5];
        interpreter.run(input, output);

        // Tìm index có xác suất cao nhất
        int maxIndex = 0;
        float maxValue = -1f;
        for (int i = 0; i < 5; i++) {
            if (output[0][i] > maxValue) {
                maxValue = output[0][i];
                maxIndex = i;
            }
            Log.d("AI_DEBUG", labels[i] + ": " + output[0][i]);
        }
        return labels[maxIndex];
    }
}