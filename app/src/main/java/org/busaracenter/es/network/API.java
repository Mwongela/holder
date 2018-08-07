package org.busaracenter.es.network;

import org.busaracenter.es.request.SaveAnalyticsRequest;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface API {

    @POST("busara.php")
    Call<ResponseBody> postAnalytics(@Body SaveAnalyticsRequest body);


}