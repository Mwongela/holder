package org.busaracenter.es.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.busaracenter.es.model.GroupSaving;

import java.util.List;

public class SaveGroupContributionRequest {

    @SerializedName("groupSaving")
    @Expose
    List<GroupSaving> groupSavingList;

    @SerializedName("recordsInMyPossession")
    @Expose
    List<String> recordsInMyPossession;

    @SerializedName("groups")
    @Expose
    List<String> syncGroups;

    public SaveGroupContributionRequest() {
    }

    public List<GroupSaving> getGroupSavingList() {
        return groupSavingList;
    }

    public void setGroupSavingList(List<GroupSaving> groupSavingList) {
        this.groupSavingList = groupSavingList;
    }

    public List<String> getRecordsInMyPossession() {
        return recordsInMyPossession;
    }

    public void setRecordsInMyPossession(List<String> recordsInMyPossession) {
        this.recordsInMyPossession = recordsInMyPossession;
    }

    public List<String> getSyncGroups() {
        return syncGroups;
    }

    public void setSyncGroups(List<String> syncGroups) {
        this.syncGroups = syncGroups;
    }
}
