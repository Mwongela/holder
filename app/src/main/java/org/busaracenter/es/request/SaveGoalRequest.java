package org.busaracenter.es.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.busaracenter.es.model.Goal;

import java.util.List;

public class SaveGoalRequest {

    @SerializedName("deviceBuild")
    @Expose
    String deviceBuild;

    @SerializedName("phoneNumber")
    @Expose
    String phoneNumber;

    @SerializedName("goals")
    @Expose
    List<Goal> goals;

    public SaveGoalRequest() {}

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

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }
}
