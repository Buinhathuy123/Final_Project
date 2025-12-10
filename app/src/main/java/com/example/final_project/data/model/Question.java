package com.example.final_project.data.model;

import java.util.List;

public class Question {
    private String _id;
    private int questionId;
    private String text;
    private List<Answer> answers;

    public String getId() { return _id; }
    public int getQuestionId() { return questionId; }
    public String getText() { return text; }
    public List<Answer> getAnswers() { return answers; }

    public static class Answer {
        private String text;
        private int score;

        public String getText() { return text; }
        public int getScore() { return score; }
    }
}
