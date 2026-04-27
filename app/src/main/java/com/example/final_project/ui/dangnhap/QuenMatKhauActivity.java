package com.example.final_project.ui.dangnhap;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuenMatKhauActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtNewPassword, edtConfirmPassword;
    private LinearLayout btnSend;
    private ImageView btnBack;

    private ImageView toggleNewPassword, toggleConfirmPassword;

    private ApiService apiService;

    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    private String savedEmail = "";
    private String savedUsername = "";
    private String savedNewPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quenmatkhau);

        // ===== MAP VIEW =====
        edtUsername = findViewById(R.id.user_name);
        edtEmail = findViewById(R.id.email_user);
        edtNewPassword = findViewById(R.id.user_new_password);
        edtConfirmPassword = findViewById(R.id.confirm_user_new_password);

        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.r36h1tttdukv);

        toggleNewPassword = findViewById(R.id.toggle_new_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);

        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        btnSend.setOnClickListener(v -> handleSend());
        btnBack.setOnClickListener(v -> finish());

        setupToggle();
    }

    // =========================
    // TOGGLE PASSWORD
    // =========================
    private void setupToggle() {

        toggleNewPassword.setOnClickListener(v -> {
            if (isNewVisible) {
                edtNewPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                edtNewPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            edtNewPassword.setSelection(edtNewPassword.getText().length());
            isNewVisible = !isNewVisible;
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmVisible) {
                edtConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                edtConfirmPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
            isConfirmVisible = !isConfirmVisible;
        });
    }

    // =========================
    // STEP 1: VALIDATE + SEND OTP
    // =========================
    private void handleSend() {

        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // ===== CHECK =====
        if (username.isEmpty()) {
            edtUsername.setError("Không được để trống username");
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Không được để trống email");
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            edtEmail.setError("Email phải là @gmail.com");
            return;
        }

        if (newPassword.isEmpty()) {
            edtNewPassword.setError("Không được để trống mật khẩu");
            return;
        }

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Không được để trống xác nhận");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không trùng");
            return;
        }

        if (newPassword.length() < 8) {
            edtNewPassword.setError("Ít nhất 8 ký tự");
            return;
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            edtNewPassword.setError("Phải có chữ in hoa");
            return;
        }

        if (!newPassword.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            edtNewPassword.setError("Phải có ký tự đặc biệt");
            return;
        }

        // ===== SAVE DATA =====
        savedEmail = email;
        savedUsername = username;
        savedNewPassword = newPassword;

        // ===== CALL SEND OTP =====
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);

        apiService.sendOtpForgot(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body().isOk()) {
                    Toast.makeText(QuenMatKhauActivity.this, "OTP đã gửi", Toast.LENGTH_SHORT).show();
                    showOtpDialog();
                } else {
                    Toast.makeText(QuenMatKhauActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(QuenMatKhauActivity.this, "Không kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // STEP 2: NHẬP OTP
    // =========================
    private void showOtpDialog() {

        EditText edtOtp = new EditText(this);
        edtOtp.setHint("Nhập OTP");

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận OTP")
                .setView(edtOtp)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String otp = edtOtp.getText().toString().trim();
                    verifyOtp(otp);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // =========================
    // STEP 3: VERIFY OTP
    // =========================
    private void verifyOtp(String otp) {

        Map<String, String> body = new HashMap<>();
        body.put("email", savedEmail);
        body.put("otp", otp);

        apiService.verifyOtpForgot(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body().isOk()) {
                    changePassword();
                } else {
                    Toast.makeText(QuenMatKhauActivity.this, "OTP sai", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(QuenMatKhauActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // STEP 4: CHANGE PASSWORD
    // =========================
    private void changePassword() {

        Map<String, String> body = new HashMap<>();
        body.put("username", savedUsername);
        body.put("newPassword", savedNewPassword);

        apiService.changePasswordForgot(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body().isOk()) {
                    Toast.makeText(QuenMatKhauActivity.this,
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuenMatKhauActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(QuenMatKhauActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}