package org.busaracenter.es.network;

import org.busaracenter.es.request.SaveAnalyticsRequest;
import org.busaracenter.es.request.SaveGoalRequest;
import org.busaracenter.es.request.SaveGroupContributionRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface API {

    @POST("pagestats")
    Call<ResponseBody> postAnalytics(@Body SaveAnalyticsRequest body);

    @POST("goal")
    Call<ResponseBody> postGoals(@Body SaveGoalRequest body);

    @POST("group")
    Call<ResponseBody> syncGroupSaving(@Body SaveGroupContributionRequest body);
}