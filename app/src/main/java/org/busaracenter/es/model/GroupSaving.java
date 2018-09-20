package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.UUID;

public class GroupSaving extends SugarRecord {

    @SerializedName("month")
    @Expose
    String month;

    @SerializedName("groupType")
    @Expose
    String groupType;

    @SerializedName("goalType")
    @Expose
    String goalType;

    @SerializedName("amount")
    @Expose
    double amount;

    @SerializedName("phoneNumber")
    @Expose
    String phoneNumber;

    @SerializedName("secondaryId")
    @Expose
    String remoteId = UUID.randomUUID().toString();

    boolean uploaded = true;

    public GroupSaving() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }
}
