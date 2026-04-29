package com.example.final_project.ui.dangnhap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quenmatkhau);

        edtUsername = findViewById(R.id.user_name);
        edtEmail = findViewById(R.id.email_user);
        edtNewPassword = findViewById(R.id.user_new_password);
        edtConfirmPassword = findViewById(R.id.confirm_user_new_password);

        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.r36h1tttdukv);

        toggleNewPassword = findViewById(R.id.toggle_new_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> handleChangePassword());

        setupToggle();
    }

    // =========================
    // CHANGE PASSWORD (NO OTP)
    // =========================
    private void handleChangePassword() {

        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        if (!isValidPassword(newPassword)) {
            edtNewPassword.setError("≥ 8 ký tự + chữ hoa + ký tự đặc biệt");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("newPassword", newPassword);

        apiService.changePassword(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    Toast.makeText(QuenMatKhauActivity.this,
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(QuenMatKhauActivity.this,
                            response.body() != null ? response.body().getMessage() : "Lỗi server",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(QuenMatKhauActivity.this,
                        "Không kết nối server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // PASSWORD RULE
    // =========================
    private boolean isValidPassword(String password) {

        if (password.length() < 8) return false;

        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        return hasUpper && hasSpecial;
    }

    // =========================
    // TOGGLE PASSWORD
    // =========================
    private void setupToggle() {

        toggleNewPassword.setOnClickListener(v -> {
            if (isNewVisible) {
                edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            edtNewPassword.setSelection(edtNewPassword.getText().length());
            isNewVisible = !isNewVisible;
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmVisible) {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
            isConfirmVisible = !isConfirmVisible;
        });
    }
}