package org.busaracenter.es.network;

import android.content.Context;

import org.busaracenter.es.util.Utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String BASE_URl = "http://34.211.227.26/api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URl)
                    .addConverterFactory(GsonConverterFactory.create(Utils.getGson()))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
