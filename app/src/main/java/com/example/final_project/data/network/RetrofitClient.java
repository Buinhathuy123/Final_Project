package com.example.final_project.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit retrofit;

    public static Retrofit getInstance() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
// mongodb+srv://Buinhathuy12345DB:Lienquanmoba123@cluster0.f6hfvik.mongodb.net/phq_app?retryWrites=true&w=majority
//mongodb+srv://Buinhathuy12345DB:Lienquanmoba123@cluster0.f6hfvik.mongodb.net/account?retryWrites=true&w=majority