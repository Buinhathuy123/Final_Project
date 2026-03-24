package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;
import com.example.final_project.ui.trangchu.TrangChuActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangNhapActivity extends AppCompatActivity {

    private EditText username, userpassword;
    private LinearLayout btnLogin;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap1);

        // ánh xạ view
        username = findViewById(R.id.user_name);
        userpassword = findViewById(R.id.user_password);
        btnLogin = findViewById(R.id.btn_login);

        // khởi tạo retrofit
        apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        // sự kiện click login
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {

        String user = username.getText().toString().trim();
        String pass = userpassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Account account = new Account(user, pass, "");

        Call<ApiResponse> call = apiService.login(account);

        call.enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isOk()) {

                        Toast.makeText(DangNhapActivity.this,
                                "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();

                        // chuyển sang TrangChu + gửi username
                        Intent intent = new Intent(
                                DangNhapActivity.this,
                                TrangChuActivity.class
                        );

                        intent.putExtra("username", user);

                        startActivity(intent);
                        finish();

                    } else {

                        Toast.makeText(DangNhapActivity.this,
                                apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Toast.makeText(DangNhapActivity.this,
                            "Lỗi server",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                Toast.makeText(DangNhapActivity.this,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}