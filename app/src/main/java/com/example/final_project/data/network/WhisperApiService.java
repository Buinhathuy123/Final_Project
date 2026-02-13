package com.example.final_project.data.network;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface WhisperApiService {

    @Multipart
    @POST("transcribe")
    Call<ResponseBody> uploadAudio(
            @Part MultipartBody.Part file
    );
}