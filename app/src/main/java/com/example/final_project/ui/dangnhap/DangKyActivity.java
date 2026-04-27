package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    private ImageView togglePassword, toggleConfirmPassword;
    private ImageView btnBack;

    private AccountRepository repository;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        confirmPassword = findViewById(R.id.user_confirm_password); // ✅ thêm
        email = findViewById(R.id.user_email);
        btnRegister = findViewById(R.id.btn_register);

        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password); // ✅ thêm

        btnBack = findViewById(R.id.btn_back);

        repository = new AccountRepository();

        btnRegister.setOnClickListener(v -> register());

        setupTogglePassword();
        setupToggleConfirmPassword();

        btnBack.setOnClickListener(v -> finish());
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

    // =========================
    // TOGGLE CONFIRM PASSWORD
    // =========================
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

    // =========================
    // REGISTER
    // =========================
    private void register() {

        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim(); // ✅ lấy confirm
        String mail = email.getText().toString().trim();

        // ===== 1. CHECK RỖNG =====
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

        // ===== 2. CHECK MATCH PASSWORD =====
        if (!pass.equals(confirmPass)) {
            confirmPassword.setError("Vui lòng kiểm tra lại xác nhận mật khẩu");
            return;
        }

        // ===== 3. USERNAME =====
        if (!user.matches(".*[a-zA-Z].*")) {
            username.setError("Username phải có chữ");
            return;
        }

        // ===== 4. PASSWORD RULE =====
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

        // ===== 5. EMAIL =====
        if (!mail.endsWith("@gmail.com")) {
            email.setError("Email phải là @gmail.com");
            return;
        }

        // ===== CALL API =====
        Account account = new Account(user, pass, mail);

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
    // CLEAR FIELD
    // =========================
    private void clearAllFields() {
        username.setText("");
        password.setText("");
        confirmPassword.setText("");
        email.setText("");
    }
}