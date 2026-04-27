package com.example.final_project.ui.dangnhap;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private LinearLayout btnSend, layoutOtp;
    private ImageView btnBack;

    private ImageView toggleNewPassword, toggleConfirmPassword;

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private LinearLayout btnVerify;
    private TextView txtResend;

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

        // MAIN
        edtUsername = findViewById(R.id.user_name);
        edtEmail = findViewById(R.id.email_user);
        edtNewPassword = findViewById(R.id.user_new_password);
        edtConfirmPassword = findViewById(R.id.confirm_user_new_password);

        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.r36h1tttdukv);

        // OTP POPUP
        layoutOtp = findViewById(R.id.layoutOtp);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        btnVerify = findViewById(R.id.btnVerify);
        txtResend = findViewById(R.id.txtResend);

        toggleNewPassword = findViewById(R.id.toggle_new_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        btnSend.setOnClickListener(v -> handleSend());
        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> verifyOtpFromPopup());

        setupToggle();
        setupOtpInputs(); // ⭐ NEW
    }

    // =========================
    // OTP AUTO MOVE + DELETE
    // =========================
    private void setupOtpInputs() {

        EditText[] inputs = new EditText[]{otp1, otp2, otp3, otp4, otp5, otp6};

        for (int i = 0; i < inputs.length; i++) {

            final int index = i;

            inputs[i].setInputType(InputType.TYPE_CLASS_NUMBER);

            inputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s.length() == 1 && index < inputs.length - 1) {
                        inputs[index + 1].requestFocus();
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });

            inputs[i].setOnKeyListener((v, keyCode, event) -> {

                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_DEL &&
                        inputs[index].getText().toString().isEmpty() &&
                        index > 0) {

                    inputs[index - 1].setText("");
                    inputs[index - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
    }

    // =========================
    // SHOW OTP POPUP
    // =========================
    private void showOtpPopup() {
        layoutOtp.setVisibility(View.VISIBLE);
        otp1.requestFocus();
    }

    // =========================
    // GET OTP FROM UI
    // =========================
    private void verifyOtpFromPopup() {

        String otp =
                otp1.getText().toString() +
                        otp2.getText().toString() +
                        otp3.getText().toString() +
                        otp4.getText().toString() +
                        otp5.getText().toString() +
                        otp6.getText().toString();

        if (otp.length() < 6) {
            Toast.makeText(this, "Nhập đủ OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyOtp(otp);
    }

    // =========================
    // SEND OTP SUCCESS
    // =========================
    private void handleSend() {

        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không trùng");
            return;
        }

        savedEmail = email;
        savedUsername = username;
        savedNewPassword = newPassword;

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);

        apiService.sendOtpForgot(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    Toast.makeText(QuenMatKhauActivity.this, "OTP đã gửi", Toast.LENGTH_SHORT).show();
                    showOtpPopup();
                } else {
                    Toast.makeText(QuenMatKhauActivity.this,
                            response.body() != null ? response.body().getMessage() : "Lỗi server",
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
    // VERIFY OTP
    // =========================
    private void verifyOtp(String otp) {

        Map<String, String> body = new HashMap<>();
        body.put("email", savedEmail);
        body.put("otp", otp);

        apiService.verifyOtpForgot(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    layoutOtp.setVisibility(View.GONE);
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
    // CHANGE PASSWORD
    // =========================
    private void changePassword() {

        Map<String, String> body = new HashMap<>();
        body.put("username", savedUsername);
        body.put("newPassword", savedNewPassword);

        apiService.changePasswordForgot(body).enqueue(new Callback<ApiResponse>() {
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
                Toast.makeText(QuenMatKhauActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
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