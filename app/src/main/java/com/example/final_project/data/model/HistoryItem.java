package com.example.final_project.data.model;

import com.google.gson.annotations.SerializedName;

public class HistoryItem {

    @SerializedName("score")
    private int score;

    @SerializedName("level")
    private String level;

    @SerializedName("date")
    private String date;

    @SerializedName("quizScore")
    private int quizScore;

    @SerializedName("voiceResult")
    private int voiceResult;

    @SerializedName("faceResult")
    private boolean faceResult;

    public int getScore() { return score; }
    public String getLevel() { return level; }
    public String getDate() { return date; }

    public int getQuizScore() { return quizScore; }
    public int getVoiceResult() { return voiceResult; }
    public boolean isFaceResult() { return faceResult; }

    public void setScore(int score) { this.score = score; }
    public void setLevel(String level) { this.level = level; }
    public void setDate(String date) { this.date = date; }

    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }
    public void setVoiceResult(int voiceResult) { this.voiceResult = voiceResult; }
    public void setFaceResult(boolean faceResult) { this.faceResult = faceResult; }
}