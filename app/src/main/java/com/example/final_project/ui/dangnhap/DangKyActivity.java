package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.repository.AccountRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private EditText username, password, confirmPassword, email;
    private LinearLayout btnRegister;
    private ImageView togglePassword, toggleConfirmPassword, btnBack;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private AccountRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        initView();

        repository = new AccountRepository();

        btnRegister.setOnClickListener(v -> handleRegister());
        btnBack.setOnClickListener(v -> finish());

        setupTogglePassword();
        setupToggleConfirmPassword();
    }

    private void initView() {
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        confirmPassword = findViewById(R.id.user_confirm_password);
        email = findViewById(R.id.user_email);

        btnRegister = findViewById(R.id.btn_register);

        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);
        btnBack = findViewById(R.id.btn_back);
    }

    // =========================
    // REGISTER
    // =========================
    private void handleRegister() {

        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();
        String mail = email.getText().toString().trim();

        // ===== VALIDATE =====
        if (user.isEmpty()) {
            username.setError("Không được để trống username");
            return;
        }

        if (!user.matches(".*[a-zA-Z].*")) {
            username.setError("Username phải có chữ");
            return;
        }

        if (pass.isEmpty()) {
            password.setError("Không được để trống password");
            return;
        }

        if (pass.length() < 8) {
            password.setError("Password phải >= 8 ký tự");
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

        if (!pass.equals(confirm)) {
            confirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        if (mail.isEmpty()) {
            email.setError("Không được để trống email");
            return;
        }

        // ===== CALL API =====
        Account account = new Account(user, pass, mail);

        btnRegister.setEnabled(false);

        repository.register(account).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                btnRegister.setEnabled(true);

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
                                res.getMessage() != null
                                        ? res.getMessage()
                                        : "Username đã tồn tại",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(DangKyActivity.this,
                            "Lỗi server",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                btnRegister.setEnabled(true);

                Toast.makeText(DangKyActivity.this,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // TOGGLE PASSWORD
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
}