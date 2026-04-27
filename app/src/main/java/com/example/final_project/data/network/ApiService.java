package com.example.final_project.data.network;

import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // API Register
    @POST("/register")
    Call<ApiResponse> register(@Body Account account);

    // API Get Questions
    @GET("questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);
    // API Login
    @POST("/login")
    Call<ApiResponse> login(@Body Account account);
    @POST("/change-password")
    Call<ApiResponse> changePassword(@Body Map<String, String> body);
    @POST("/update-result")
    Call<ApiResponse> updateResult(@Body Map<String, Object> body);
    @GET("/user/{username}")
    Call<ApiResponse> getUser(@Path("username") String username);
    @POST("send-otp")
    Call<ApiResponse> sendOtp(@Body Map<String, String> body);

    @POST("verify-otp")
    Call<ApiResponse> verifyOtp(@Body Map<String, String> body);
}