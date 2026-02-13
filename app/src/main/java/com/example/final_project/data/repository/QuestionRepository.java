package com.example.final_project.data.repository;

import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.model.Question;
import com.example.final_project.data.network.ApiService;
import com.example.final_project.data.network.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionRepository {

    public interface QuestionCallback {
        void onSuccess(List<Question> questions);
        void onFail(String error);
    }

    public void loadRandomQuestions(QuestionCallback callback) {

        ApiService api = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        api.getQuestions(9).enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(
                    Call<ApiResponse> call,
                    Response<ApiResponse> response
            ) {

                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFail("API lỗi: " + response.code());
                    return;
                }

                ApiResponse data = response.body();

                if (!data.isOk() || data.getQuestions() == null) {
                    callback.onFail("Dữ liệu API không hợp lệ");
                    return;
                }

                List<Question> questions = data.getQuestions();

                if (questions.isEmpty()) {
                    callback.onFail("Danh sách câu hỏi rỗng");
                    return;
                }

                Collections.shuffle(questions);
                callback.onSuccess(questions);
            }

            @Override
            public void onFailure(
                    Call<ApiResponse> call,
                    Throwable t
            ) {
                callback.onFail("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
