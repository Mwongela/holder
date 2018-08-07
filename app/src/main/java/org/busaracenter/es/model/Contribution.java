package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Contribution extends SugarRecord {

    @SerializedName("amount")
    @Expose
    double amount = 0;

    Goal goal;

    @SerializedName("timestamp")
    @Expose
    long timestamp = System.currentTimeMillis();

    @SerializedName("contributionVehicle")
    @Expose
    String contributionVehicle;

    public Contribution() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContributionVehicle() {
        return contributionVehicle;
    }

    public void setContributionVehicle(String contributionVehicle) {
        this.contributionVehicle = contributionVehicle;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }
}
