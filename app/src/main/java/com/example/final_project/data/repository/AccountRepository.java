package com.example.final_project.data.repository;

import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;

public class AccountRepository {

    private ApiService apiService;

    public AccountRepository() {

        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);
    }

    public Call<Account> register(Account account) {
        return apiService.register(account);
    }

    public Call<ApiResponse> login(Account account) {
        return apiService.login(account);
    }
    public Call<ApiResponse> updateResult(Map<String, Object> body) {
        return apiService.updateResult(body);
    }
    public Call<Account> getUser(String username) {
        return apiService.getUser(username);
    }
}