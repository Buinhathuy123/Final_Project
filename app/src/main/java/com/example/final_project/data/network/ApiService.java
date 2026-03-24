package com.example.final_project.data.network;

import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // API Register
    @POST("/register")
    Call<Account> register(@Body Account account);

    // API Get Questions
    @GET("questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);
    // API Login
    @POST("/login")
    Call<ApiResponse> login(@Body Account account);
}