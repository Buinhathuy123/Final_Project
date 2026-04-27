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

    // ================= REGISTER =================
    @POST("/register")
    Call<ApiResponse> register(@Body Account account);

    // ================= QUESTIONS =================
    @GET("questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);

    // ================= LOGIN =================
    @POST("/login")
    Call<ApiResponse> login(@Body Account account);

    // ================= CHANGE PASSWORD (OLD - cần password hiện tại) =================
    @POST("/change-password")
    Call<ApiResponse> changePassword(@Body Map<String, String> body);

    // ================= UPDATE RESULT =================
    @POST("/update-result")
    Call<ApiResponse> updateResult(@Body Map<String, Object> body);

    // ================= GET USER =================
    @GET("/user/{username}")
    Call<ApiResponse> getUser(@Path("username") String username);

    // ================= OTP REGISTER (GIỮ NGUYÊN) =================
    @POST("/send-otp")
    Call<ApiResponse> sendOtp(@Body Map<String, String> body);

    @POST("/verify-otp")
    Call<ApiResponse> verifyOtp(@Body Map<String, String> body);

    // =========================================================
    // 🔥 OTP QUÊN MẬT KHẨU (MỚI - KHÔNG ẢNH HƯỞNG REGISTER)
    // =========================================================

    // 1. Gửi OTP (check username + email)
    @POST("/forgot-password/send-otp")
    Call<ApiResponse> sendOtpForgot(@Body Map<String, String> body);

    // 2. Verify OTP
    @POST("/forgot-password/verify-otp")
    Call<ApiResponse> verifyOtpForgot(@Body Map<String, String> body);

    // 3. Đổi mật khẩu KHÔNG cần password cũ
    @POST("/forgot-password/change-password")
    Call<ApiResponse> changePasswordForgot(@Body Map<String, String> body);
}