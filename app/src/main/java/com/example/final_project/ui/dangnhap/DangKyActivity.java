package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;
import com.example.final_project.data.repository.AccountRepository;

import java.util.HashMap;
import java.util.Map;

import retrofit2.*;

public class DangKyActivity extends AppCompatActivity {

    private EditText username, password, confirmPassword, email;
    private LinearLayout btnRegister, btnVerify;
    private ImageView togglePassword, toggleConfirmPassword, btnBack;

    private LinearLayout layoutOtp;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private TextView txtResend;

    private ApiService apiService;
    private AccountRepository repository;

    private String tempUser, tempPass, tempEmail;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private boolean isSendingOtp = false;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        initView();

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        repository = new AccountRepository();

        btnRegister.setOnClickListener(v -> validateAndSendOtp());
        btnVerify.setOnClickListener(v -> verifyOtp());
        btnBack.setOnClickListener(v -> finish());

        setupTogglePassword();
        setupToggleConfirmPassword();
        setupOtpInput();
    }

    private void initView() {
        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        confirmPassword = findViewById(R.id.user_confirm_password);
        email = findViewById(R.id.user_email);

        btnRegister = findViewById(R.id.btn_register);
        btnVerify = findViewById(R.id.btnVerify);

        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);
        btnBack = findViewById(R.id.btn_back);

        layoutOtp = findViewById(R.id.layoutOtp);
        txtResend = findViewById(R.id.txtResend);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
    }

    // =========================
    // SEND OTP
    // =========================
    private void validateAndSendOtp() {

        if (isSendingOtp) return;

        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();
        String mail = email.getText().toString().trim();

        if (user.isEmpty()) {
            username.setError("Không được để trống username");
            return;
        }

        if (pass.length() < 8) {
            password.setError("Password >= 8 ký tự");
            return;
        }

        if (!pass.equals(confirmPass)) {
            confirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        if (!mail.endsWith("@gmail.com")) {
            email.setError("Email phải là gmail");
            return;
        }

        tempUser = user;
        tempPass = pass;
        tempEmail = mail;

        isSendingOtp = true;
        btnRegister.setEnabled(false);

        Map<String, String> body = new HashMap<>();
        body.put("email", mail);

        apiService.sendOtp(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                isSendingOtp = false;
                btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {

                    Toast.makeText(DangKyActivity.this, "Đã gửi OTP", Toast.LENGTH_SHORT).show();

                    layoutOtp.setVisibility(View.VISIBLE);

                    clearOtp();
                    startCountdown();

                } else {
                    Toast.makeText(DangKyActivity.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                isSendingOtp = false;
                btnRegister.setEnabled(true);

                Toast.makeText(DangKyActivity.this, "Không kết nối server", Toast.LENGTH_SHORT).show();
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

        if (otp.length() != 6) {
            Toast.makeText(this, "OTP phải đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", tempEmail);
        body.put("otp", otp);

        apiService.verifyOtp(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
                    register();
                } else {
                    Toast.makeText(DangKyActivity.this, "OTP sai", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DangKyActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // REGISTER
    // =========================
    private void register() {

        Account account = new Account(tempUser, tempPass, tempEmail);

        repository.register(account).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.body() != null && response.body().isOk()) {

                    Toast.makeText(DangKyActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(DangKyActivity.this, DangNhapActivity.class));
                    finish();

                } else {
                    Toast.makeText(DangKyActivity.this, "Username đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DangKyActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================
    // OTP UI
    // =========================
    private void clearOtp() {
        otp1.setText(""); otp2.setText(""); otp3.setText("");
        otp4.setText(""); otp5.setText(""); otp6.setText("");
        otp1.requestFocus();
    }

    private void startCountdown() {

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(30000, 1000) {

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

    private void setupOtpInput() {
        otp1.addTextChangedListener(new SimpleTextWatcher(() -> otp2.requestFocus()));
        otp2.addTextChangedListener(new SimpleTextWatcher(() -> otp3.requestFocus()));
        otp3.addTextChangedListener(new SimpleTextWatcher(() -> otp4.requestFocus()));
        otp4.addTextChangedListener(new SimpleTextWatcher(() -> otp5.requestFocus()));
        otp5.addTextChangedListener(new SimpleTextWatcher(() -> otp6.requestFocus()));
    }

    class SimpleTextWatcher implements TextWatcher {
        Runnable next;
        SimpleTextWatcher(Runnable next) { this.next = next; }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) next.run();
        }
    }

    private void setupTogglePassword() {
        togglePassword.setOnClickListener(v -> {
            password.setInputType(isPasswordVisible ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            password.setSelection(password.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void setupToggleConfirmPassword() {
        toggleConfirmPassword.setOnClickListener(v -> {
            confirmPassword.setInputType(isConfirmPasswordVisible ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPassword.setSelection(confirmPassword.getText().length());
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
        });
    }
}