package org.busaracenter.es.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.busaracenter.es.model.PageStat;

import java.util.List;

public class SaveAnalyticsRequest {

    @SerializedName("pageStats")
    @Expose
    List<PageStat> pageStats;

    @SerializedName("deviceBuild")
    @Expose
    String deviceBuild;

    @SerializedName("phoneNumber")
    @Expose
    String phoneNumber;

    public SaveAnalyticsRequest() {} // Default constructor

    public List<PageStat> getPageStats() {
        return pageStats;
    }

    public void setPageStats(List<PageStat> pageStats) {
        this.pageStats = pageStats;
    }

    public String getDeviceBuild() {
        return deviceBuild;
    }

    public void setDeviceBuild(String deviceBuild) {
        this.deviceBuild = deviceBuild;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
