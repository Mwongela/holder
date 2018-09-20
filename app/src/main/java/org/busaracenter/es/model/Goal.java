package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

public class Goal extends SugarRecord {

    @SerializedName("month")
    @Expose
    String month;

    @SerializedName("goalType")
    @Expose
    String goalType;

    @SerializedName("groupType")
    @Expose
    String groupType;

    @SerializedName("goalNote")
    @Expose
    String goalNote;

    @SerializedName("timestamp")
    @Expose
    long createdAt = System.currentTimeMillis();

    @SerializedName("allowNotifications")
    @Expose
    boolean allowNotification = true;

    @SerializedName("uploaded")
    @Expose
    boolean uploaded = false;

    @SerializedName("contributions")
    @Expose
    @Ignore
    List<Contribution> contributions;

    public Goal() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGoalNote() {
        return goalNote;
    }

    public void setGoalNote(String goalNote) {
        this.goalNote = goalNote;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public double getTotalContributions() {
        double total = 0;

        try {

            List<Contribution> contributions = Contribution.find(Contribution.class, "GOAL=?", this.id.toString());
            for (Contribution contribution : contributions) {
                total += contribution.getAmount();
            }

        } catch (Exception ex) {

        }

        return total;
    }
}
