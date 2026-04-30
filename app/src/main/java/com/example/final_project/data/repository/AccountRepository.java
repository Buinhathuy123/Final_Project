package com.example.final_project.data.repository;

import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;

public class AccountRepository {

    private final ApiService apiService;

    public AccountRepository() {
        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);
    }

    // =========================
    // REGISTER
    // =========================
    public Call<ApiResponse> register(Account account) {
        return apiService.register(account);
    }

    // =========================
    // LOGIN
    // =========================
    public Call<ApiResponse> login(Account account) {
        return apiService.login(account);
    }

    // =========================
    // GET USER (🔥 load từ server)
    // =========================
    public Call<ApiResponse> getUser(String username) {
        return apiService.getUser(username);
    }

    // =========================
    // CHANGE PASSWORD
    // =========================
    public Call<ApiResponse> changePassword(Map<String, String> body) {
        return apiService.changePassword(body);
    }

    // =========================
    // UPDATE RESULT (🔥 quan trọng)
    // =========================
    public Call<ApiResponse> updateResult(Map<String, Object> body) {
        return apiService.updateResult(body);
    }
}