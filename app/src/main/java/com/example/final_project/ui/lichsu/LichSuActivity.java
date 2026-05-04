package com.example.final_project.ui.lichsu;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.data.model.Account;
import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.model.HistoryItem;
import com.example.final_project.data.repository.AccountRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LichSuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView txtNoData;
    private ImageView btnBack;

    private AccountRepository repo;
    private String username;

    private List<HistoryItem> historyList = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lichsu);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        txtNoData = findViewById(R.id.txtNoData);
        btnBack = findViewById(R.id.btnBack);

        repo = new AccountRepository();
        username = getIntent().getStringExtra("username");

        adapter = new HistoryAdapter(historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {

        if (username == null || username.isEmpty()) {
            showEmpty();
            return;
        }

        repo.getUser(username).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    showEmpty();
                    return;
                }

                ApiResponse res = response.body();

                if (!res.isOk() || res.getData() == null) {
                    showEmpty();
                    return;
                }

                Account user = res.getData();

                List<HistoryItem> list = user.getHistory();

                if (list != null && !list.isEmpty()) {

                    historyList.clear();
                    historyList.addAll(list);

                    adapter.notifyDataSetChanged();

                    recyclerView.setVisibility(View.VISIBLE);
                    txtNoData.setVisibility(View.GONE);

                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        recyclerView.setVisibility(View.GONE);
        txtNoData.setVisibility(View.VISIBLE);
    }
}