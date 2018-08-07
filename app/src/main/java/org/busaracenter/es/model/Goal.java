package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class Goal extends SugarRecord {

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

    public Goal() {}

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
}
