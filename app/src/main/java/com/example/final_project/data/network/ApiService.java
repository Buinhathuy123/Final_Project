package com.example.final_project.data.network;

import com.example.final_project.data.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("questions")
    Call<ApiResponse> getQuestions(@Query("size") int size);
}
