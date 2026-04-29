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
    @GET("/questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);

    // ================= LOGIN =================
    @POST("/login")
    Call<ApiResponse> login(@Body Account account);

    // ================= CHANGE PASSWORD (CÓ PASSWORD CŨ) =================
    @POST("/change-password")
    Call<ApiResponse> changePassword(@Body Map<String, String> body);

    // ================= UPDATE RESULT =================
    @POST("/update-result")
    Call<ApiResponse> updateResult(@Body Map<String, Object> body);

    // ================= GET USER =================
    @GET("/user/{username}")
    Call<ApiResponse> getUser(@Path("username") String username);

    // =========================================================
    // 🔐 OTP ĐĂNG KÝ
    // =========================================================

    // Gửi OTP
    @POST("/send-otp")
    Call<ApiResponse> sendOtp(@Body Map<String, String> body);

    // Verify OTP
    @POST("/verify-otp")
    Call<ApiResponse> verifyOtp(@Body Map<String, String> body);

    // =========================================================
    // 🔥 OTP QUÊN MẬT KHẨU (TÁCH RIÊNG HOÀN TOÀN)
    // =========================================================

    // 1. Gửi OTP (check username + email)
    @POST("/send-otp-forgot")
    Call<ApiResponse> sendOtpForgot(@Body Map<String, String> body);

    // 2. Verify OTP riêng
    @POST("/verify-otp-forgot")
    Call<ApiResponse> verifyOtpForgot(@Body Map<String, String> body);

    // 3. Đổi mật khẩu KHÔNG cần password cũ
    @POST("/change-password-forgot")
    Call<ApiResponse> changePasswordForgot(@Body Map<String, String> body);
}