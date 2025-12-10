package com.example.final_project.data.model;

import java.util.List;

public class ApiResponse {
    private boolean ok;
    private int count;
    private List<Question> questions;

    public boolean isOk() {
        return ok;
    }

    public int getCount() {
        return count;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
