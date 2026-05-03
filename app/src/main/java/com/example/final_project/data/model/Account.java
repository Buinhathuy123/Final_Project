package com.example.final_project.data.model;

import com.google.gson.annotations.SerializedName;

public class Account {

    private String username;
    private String password;
    private String email;

    private Integer finalScore;
    private String level;
    private String lastTestTime;

    // --- BỔ SUNG: 3 đầu điểm chi tiết ---
    // Sử dụng @SerializedName để đảm bảo khớp với key từ API (nếu Backend trả về kiểu snake_case)
    @SerializedName("quizScore")
    private int quizScore;

    @SerializedName("voiceResult")
    private int voiceResult;

    @SerializedName("faceResult")
    private boolean faceResult;

    // Constructor
    public Account(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // --- GETTER & SETTER MẶC ĐỊNH ---

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLastTestTime() {
        return lastTestTime;
    }

    public void setLastTestTime(String lastTestTime) {
        this.lastTestTime = lastTestTime;
    }

    // --- BỔ SUNG: Getter & Setter cho các chỉ số chi tiết ---

    public int getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(int quizScore) {
        this.quizScore = quizScore;
    }

    public int getVoiceResult() {
        return voiceResult;
    }

    public void setVoiceResult(int voiceResult) {
        this.voiceResult = voiceResult;
    }

    public boolean isFaceResult() { // Kiểu boolean thường dùng 'is' thay vì 'get'
        return faceResult;
    }

    public void setFaceResult(boolean faceResult) {
        this.faceResult = faceResult;
    }
}