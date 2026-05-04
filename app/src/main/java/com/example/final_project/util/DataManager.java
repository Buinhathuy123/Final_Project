package com.example.final_project.util;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {

    private static final String PREF_NAME = "CareSenseData";

    // =========================
    // CURRENT USER
    // =========================
    public static void setCurrentUser(Context context, String username) {
        getPrefs(context).edit().putString("CURRENT_USER", username).apply();
    }

    public static String getCurrentUser(Context context) {
        return getPrefs(context).getString("CURRENT_USER", "default");
    }

    private static String key(Context context, String base) {
        String user = getCurrentUser(context);
        return user + "_" + base;
    }

    // ==========================================================
    // LƯU KẾT QUẢ CUỐI CÙNG (Để không bị mất khi Reset bài test mới)
    // ==========================================================
    public static void saveLastFullResult(Context context, int score, String level, int quiz, int voice, boolean face) {
        getPrefs(context).edit()
                .putInt(key(context, "LAST_SCORE"), score)
                .putString(key(context, "LAST_LEVEL"), level)
                .putInt(key(context, "LAST_QUIZ"), quiz)
                .putInt(key(context, "LAST_VOICE"), voice)
                .putBoolean(key(context, "LAST_FACE"), face)
                .apply();
    }

    // ==========================================================
    // GET LAST RESULT (Bổ sung để gọi từ TrangChuActivity)
    // ==========================================================
    public static int getLastScore(Context context) {
        return getPrefs(context).getInt(key(context, "LAST_SCORE"), -1);
    }

    public static String getLastLevel(Context context) {
        return getPrefs(context).getString(key(context, "LAST_LEVEL"), "");
    }

    public static int getLastQuiz(Context context) {
        return getPrefs(context).getInt(key(context, "LAST_QUIZ"), 0);
    }

    public static int getLastVoice(Context context) {
        return getPrefs(context).getInt(key(context, "LAST_VOICE"), 0);
    }

    public static boolean getLastFace(Context context) {
        return getPrefs(context).getBoolean(key(context, "LAST_FACE"), false);
    }

    // ==========================================================
    // RESET PROGRESS (Chỉ xóa dữ liệu đang làm dở, giữ lại LAST_RESULT)
    // ==========================================================
    public static void resetProgressForNewTest(Context context) {
        getPrefs(context).edit()
                .remove(key(context, "IS_QUIZ_DONE"))
                .remove(key(context, "IS_VOICE_DONE"))
                .remove(key(context, "IS_FACE_DONE"))
                .putInt(key(context, "QUIZ"), 0)
                .putInt(key(context, "VOICE"), -1)
                .putBoolean(key(context, "FACE"), false)
                .apply();
    }

    // =========================
    // SAVE TEMP DATA (Dữ liệu đang làm dở)
    // =========================
    public static void saveQuizScore(Context context, int score) {
        getPrefs(context).edit()
                .putInt(key(context, "QUIZ"), score)
                .putBoolean(key(context, "IS_QUIZ_DONE"), true)
                .apply();
    }

    public static void saveVoiceResult(Context context, int label) {
        getPrefs(context).edit()
                .putInt(key(context, "VOICE"), label)
                .putBoolean(key(context, "IS_VOICE_DONE"), true)
                .apply();
    }

    public static void saveFaceResult(Context context, boolean isDepressed) {
        getPrefs(context).edit()
                .putBoolean(key(context, "FACE"), isDepressed)
                .putBoolean(key(context, "IS_FACE_DONE"), true)
                .apply();
    }

    // =========================
    // GET TEMP DATA
    // =========================
    public static int getQuizScore(Context context) {
        return getPrefs(context).getInt(key(context, "QUIZ"), 0);
    }

    public static int getVoiceResult(Context context) {
        return getPrefs(context).getInt(key(context, "VOICE"), -1);
    }

    public static boolean getFaceResult(Context context) {
        return getPrefs(context).getBoolean(key(context, "FACE"), false);
    }

    // =========================
    // CHECK COMPLETED
    // =========================
    public static boolean isQuizCompleted(Context context) {
        return getPrefs(context).getBoolean(key(context, "IS_QUIZ_DONE"), false);
    }

    public static boolean isVoiceCompleted(Context context) {
        return getPrefs(context).getBoolean(key(context, "IS_VOICE_DONE"), false);
    }

    public static boolean isFaceCompleted(Context context) {
        return getPrefs(context).getBoolean(key(context, "IS_FACE_DONE"), false);
    }

    // =========================
    // CẤU HÌNH HỆ THỐNG
    // =========================
    public static void clearAllData(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}