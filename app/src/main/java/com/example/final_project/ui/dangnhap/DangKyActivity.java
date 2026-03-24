package com.example.final_project.ui.dangnhap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.repository.AccountRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyActivity extends AppCompatActivity {

    private EditText username, password, email;
    private LinearLayout btnRegister;

    private AccountRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        username = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        email = findViewById(R.id.user_email);
        btnRegister = findViewById(R.id.btn_register);

        repository = new AccountRepository();

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {

        String user = username.getText().toString();
        String pass = password.getText().toString();
        String mail = email.getText().toString();

        Account account = new Account(user, pass, mail);

        repository.register(account).enqueue(new Callback<Account>() {

            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(DangKyActivity.this,
                            "Register success",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(
                            DangKyActivity.this,
                            DangNhapActivity.class
                    );
                    startActivity(intent);

                    // đóng màn hình đăng ký (không quay lại được)
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {

                Toast.makeText(DangKyActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}