package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;
import com.example.final_project.data.repository.AccountRepository;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private EditText username, password, confirmPassword, email;
    private LinearLayout btnRegister;
    private ImageView togglePassword, toggleConfirmPassword;
    private ImageView btnBack;

    // ===== OTP =====
    private LinearLayout layoutOtp, btnVerify;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private TextView txtResend;

    private AccountRepository repository;
    private ApiService apiService;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // lưu tạm trước khi verify OTP
    private String tempUser, tempPass, tempEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        // ===== FORM =====
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        confirmPassword = findViewById(R.id.user_confirm_password);
        email = findViewById(R.id.user_email);
        btnRegister = findViewById(R.id.btn_register);

        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);
        btnBack = findViewById(R.id.btn_back);

        // ===== OTP =====
        layoutOtp = findViewById(R.id.layoutOtp);
        btnVerify = findViewById(R.id.btnVerify);
        txtResend = findViewById(R.id.txtResend);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        repository = new AccountRepository();
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        btnRegister.setOnClickListener(v -> validateAndSendOtp());
        btnVerify.setOnClickListener(v -> verifyOtp());

        btnBack.setOnClickListener(v -> finish());

        setupTogglePassword();
        setupToggleConfirmPassword();
        startCountdown();
    }

    // =========================
    // GIỮ NGUYÊN VALIDATE + THÊM SEND OTP
    // =========================
    private void validateAndSendOtp() {

        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();
        String mail = email.getText().toString().trim();

        // ===== GIỮ NGUYÊN VALIDATE =====
        if (user.isEmpty()) {
            username.setError("Không được để trống username");
            return;
        }

        if (pass.isEmpty()) {
            password.setError("Không được để trống password");
            return;
        }

        if (confirmPass.isEmpty()) {
            confirmPassword.setError("Không được để trống xác nhận mật khẩu");
            return;
        }

        if (mail.isEmpty()) {
            email.setError("Không được để trống email");
            return;
        }

        if (!pass.equals(confirmPass)) {
            confirmPassword.setError("Vui lòng kiểm tra lại xác nhận mật khẩu");
            return;
        }

        if (!user.matches(".*[a-zA-Z].*")) {
            username.setError("Username phải có chữ");
            return;
        }

        if (pass.length() < 8) {
            password.setError("Password phải ít nhất 8 ký tự");
            return;
        }

        if (!pass.matches(".*[A-Z].*")) {
            password.setError("Phải có chữ in hoa");
            return;
        }

        if (!pass.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            password.setError("Phải có ký tự đặc biệt");
            return;
        }

        if (!mail.endsWith("@gmail.com")) {
            email.setError("Email phải là @gmail.com");
            return;
        }

        // ===== LƯU TẠM =====
        tempUser = user;
        tempPass = pass;
        tempEmail = mail;

        // ===== GỬI OTP =====
        Map<String, String> body = new HashMap<>();
        body.put("email", mail);

        apiService.sendOtp(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {

                    Toast.makeText(DangKyActivity.this,
                            "Đã gửi OTP",
                            Toast.LENGTH_SHORT).show();

                    layoutOtp.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(DangKyActivity.this,
                            "Gửi OTP thất bại",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DangKyActivity.this,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // VERIFY OTP
    // =========================
    private void verifyOtp() {

        String otp = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString() +
                otp5.getText().toString() +
                otp6.getText().toString();

        Map<String, String> body = new HashMap<>();
        body.put("email", tempEmail);
        body.put("otp", otp);

        apiService.verifyOtp(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {

                    register(); // ✅ CHỈ ĐĂNG KÝ SAU KHI OTP ĐÚNG

                } else {
                    Toast.makeText(DangKyActivity.this,
                            "Vui lòng kiểm tra lại OTP",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DangKyActivity.this,
                        "Lỗi mạng",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // REGISTER (KHÔNG ĐỔI)
    // =========================
    private void register() {

        Account account = new Account(tempUser, tempPass, tempEmail);

        repository.register(account).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse res = response.body();

                    if (res.isOk()) {

                        Toast.makeText(DangKyActivity.this,
                                "Đăng ký thành công",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
                                DangKyActivity.this,
                                DangNhapActivity.class
                        ));
                        finish();

                    } else {
                        Toast.makeText(DangKyActivity.this,
                                "Vui lòng chọn tên tài khoản khác",
                                Toast.LENGTH_SHORT).show();

                        clearAllFields();
                    }

                } else {
                    Toast.makeText(DangKyActivity.this,
                            "Lỗi server",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                Toast.makeText(DangKyActivity.this,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // RESEND OTP
    // =========================
    private void startCountdown() {

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                txtResend.setText("Gửi lại sau " + millisUntilFinished / 1000 + "s");
                txtResend.setEnabled(false);
            }

            public void onFinish() {
                txtResend.setText("Gửi lại OTP");
                txtResend.setEnabled(true);

                txtResend.setOnClickListener(v -> validateAndSendOtp());
            }

        }.start();
    }

    // =========================
    // TOGGLE PASSWORD (GIỮ NGUYÊN)
    // =========================
    private void setupTogglePassword() {
        togglePassword.setOnClickListener(v -> {

            if (isPasswordVisible) {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            password.setSelection(password.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void setupToggleConfirmPassword() {
        toggleConfirmPassword.setOnClickListener(v -> {

            if (isConfirmPasswordVisible) {
                confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            confirmPassword.setSelection(confirmPassword.getText().length());
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });
    }

    private void clearAllFields() {
        username.setText("");
        password.setText("");
        confirmPassword.setText("");
        email.setText("");
    }
}