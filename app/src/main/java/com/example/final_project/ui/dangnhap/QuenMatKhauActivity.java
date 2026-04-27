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

    private EditText edtUsername, edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private LinearLayout btnSend;
    private ImageView btnBack;

    private ImageView toggleCurrentPassword, toggleNewPassword, toggleConfirmPassword;

    private ApiService apiService;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quenmatkhau);

        edtUsername = findViewById(R.id.user_name);
        edtCurrentPassword = findViewById(R.id.user_password); // 🔥 thêm
        edtNewPassword = findViewById(R.id.user_new_password);
        edtConfirmPassword = findViewById(R.id.confirm_user_new_password);

        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.r36h1tttdukv);

        toggleCurrentPassword = findViewById(R.id.toggle_user_password); // 🔥 thêm
        toggleNewPassword = findViewById(R.id.toggle_new_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);

        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        btnSend.setOnClickListener(v -> changePassword());
        btnBack.setOnClickListener(v -> finish());

        setupToggle();
    }

    // =========================
    // TOGGLE PASSWORD
    // =========================
    private void setupToggle() {

        toggleCurrentPassword.setOnClickListener(v -> {
            if (isCurrentVisible) {
                edtCurrentPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                edtCurrentPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            edtCurrentPassword.setSelection(edtCurrentPassword.getText().length());
            isCurrentVisible = !isCurrentVisible;
        });

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
    // CHANGE PASSWORD
    // =========================
    private void changePassword() {

        String username = edtUsername.getText().toString().trim();
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // ===== CHECK RỖNG =====
        if (username.isEmpty()) {
            edtUsername.setError("Không được để trống username");
            return;
        }

        if (currentPassword.isEmpty()) {
            edtCurrentPassword.setError("Không được để trống mật khẩu hiện tại");
            return;
        }

        if (newPassword.isEmpty()) {
            edtNewPassword.setError("Không được để trống mật khẩu mới");
            return;
        }

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Không được để trống xác nhận mật khẩu");
            return;
        }

        // ===== CHECK MATCH =====
        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng kiểm tra lại xác nhận mật khẩu");
            return;
        }

        // ===== CHECK TRÙNG PASSWORD =====
        if (newPassword.equals(currentPassword)) {
            edtNewPassword.setError("Vui lòng chọn mật khẩu khác hiện tại");
            return;
        }

        // ===== PASSWORD RULE =====
        if (newPassword.length() < 8) {
            edtNewPassword.setError("Password phải ít nhất 8 ký tự");
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

        // ===== CALL API (GỬI CẢ CURRENT PASSWORD) =====
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("currentPassword", currentPassword); // 🔥 thêm quan trọng
        body.put("newPassword", newPassword);

        apiService.changePassword(body).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuenMatKhauActivity.this,
                            "Lỗi server",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiResponse res = response.body();

                if (res.isOk()) {

                    Toast.makeText(QuenMatKhauActivity.this,
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT).show();

                    finish();

                } else {

                    // 🔥 sai mật khẩu hiện tại hoặc user
                    Toast.makeText(QuenMatKhauActivity.this,
                            "Vui lòng kiểm tra lại mật khẩu và tài khoản",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(QuenMatKhauActivity.this,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}