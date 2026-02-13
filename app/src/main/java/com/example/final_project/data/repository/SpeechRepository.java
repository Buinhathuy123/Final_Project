package com.example.final_project.data.repository;

import com.example.final_project.data.network.RetrofitClient;
import com.example.final_project.data.network.WhisperApiService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeechRepository {

    public interface SpeechCallback {
        void onSuccess(String text);
        void onFail(String error);
    }

    public void transcribeAudio(File audioFile,
                                SpeechCallback callback) {

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("audio/*"),
                        audioFile);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData(
                        "file",
                        audioFile.getName(),
                        requestFile);

        WhisperApiService api =
                RetrofitClient.getInstance()
                        .create(WhisperApiService.class);

        api.uploadAudio(body)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {

                        if (!response.isSuccessful()) {
                            callback.onFail(
                                    "Server lỗi: "
                                            + response.code());
                            return;
                        }

                        try {
                            String result =
                                    response.body().string();
                            callback.onSuccess(result);
                        } catch (Exception e) {
                            callback.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call,
                                          Throwable t) {
                        callback.onFail(t.getMessage());
                    }
                });
    }
}