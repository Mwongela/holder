package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class InputStat extends SugarRecord {

    @SerializedName("backspaceCount")
    @Expose
    private Integer backspaceCount;
    @SerializedName("totalKeyPressCount")
    @Expose
    private Integer totalKeyPressCount;
    @SerializedName("timeStartTyping")
    @Expose
    private Long timeStartTyping;
    @SerializedName("timeStopTyping")
    @Expose
    private Long timeStopTyping;
    @SerializedName("timeSpentInField")
    @Expose
    private Long timeSpentInField;
    @SerializedName("finalInputValue")
    @Expose
    private String finalInputValue;
    @SerializedName("finalInputLength")
    @Expose
    private Integer finalInputLength;
    @SerializedName("intelliWordChanges")
    @Expose
    private String intelliWordChanges;
    @SerializedName("intelliWordIndex")
    @Expose
    private Integer intelliWordIndex;
    @SerializedName("name")
    @Expose
    private String name;

    private Long localRecordId;

    public InputStat() {}

    public Integer getBackspaceCount() {
        return backspaceCount;
    }

    public void setBackspaceCount(Integer backspaceCount) {
        this.backspaceCount = backspaceCount;
    }

    public Integer getTotalKeyPressCount() {
        return totalKeyPressCount;
    }

    public void setTotalKeyPressCount(Integer totalKeyPressCount) {
        this.totalKeyPressCount = totalKeyPressCount;
    }

    public Long getTimeStartTyping() {
        return timeStartTyping;
    }

    public void setTimeStartTyping(Long timeStartTyping) {
        this.timeStartTyping = timeStartTyping;
    }

    public Long getTimeStopTyping() {
        return timeStopTyping;
    }

    public void setTimeStopTyping(Long timeStopTyping) {
        this.timeStopTyping = timeStopTyping;
    }

    public Long getTimeSpentInField() {
        return timeSpentInField;
    }

    public void setTimeSpentInField(Long timeSpentInField) {
        this.timeSpentInField = timeSpentInField;
    }

    public String getFinalInputValue() {
        return finalInputValue;
    }

    public void setFinalInputValue(String finalInputValue) {
        this.finalInputValue = finalInputValue;
    }

    public Integer getFinalInputLength() {
        return finalInputLength;
    }

    public void setFinalInputLength(Integer finalInputLength) {
        this.finalInputLength = finalInputLength;
    }

    public String getIntelliWordChanges() {
        return intelliWordChanges;
    }

    public void setIntelliWordChanges(String intelliWordChanges) {
        this.intelliWordChanges = intelliWordChanges;
    }

    public Integer getIntelliWordIndex() {
        return intelliWordIndex;
    }

    public void setIntelliWordIndex(Integer intelliWordIndex) {
        this.intelliWordIndex = intelliWordIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLocalRecordId() {
        return localRecordId;
    }

    public void setLocalRecordId(Long localRecordId) {
        this.localRecordId = localRecordId;
    }
}