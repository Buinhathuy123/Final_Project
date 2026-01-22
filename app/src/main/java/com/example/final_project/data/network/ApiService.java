package com.example.final_project.data.network;

import com.example.final_project.data.model.ApiResponse;
import com.example.final_project.data.model.SpeechResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    // ================= QUESTIONS =================
    @GET("questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);

    // ================= SPEECH TO TEXT =================
    @Multipart
    @POST("speech-to-text")
    Call<SpeechResponse> uploadAudio(
            @Part MultipartBody.Part audio
    );
}
