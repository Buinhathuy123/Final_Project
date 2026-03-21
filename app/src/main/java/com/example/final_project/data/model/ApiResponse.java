package com.example.final_project.data.model;

import java.util.List;

public class ApiResponse {

    private boolean ok;
    private String message;
    private int count;
    private Account data;
    private List<Question> questions;

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }

    public Account getData() {
        return data;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}