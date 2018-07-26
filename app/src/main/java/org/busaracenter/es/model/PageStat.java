package org.busaracenter.es.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

public class PageStat extends SugarRecord {

    @SerializedName("timeStamp")
    @Expose
    private Long timeStamp;
    @SerializedName("timeSpent")
    @Expose
    private Long timeSpent;
    @SerializedName("previousPage")
    @Expose
    private String previousPage;
    @SerializedName("pageName")
    @Expose
    private String pageName;
    @SerializedName("pageOrder")
    @Expose
    private Integer pageOrder;
    @SerializedName("isInputPresent")
    @Expose
    private String isInputPresent;
    @SerializedName("inputStats")
    @Expose
    private InputStat inputStats;

    public PageStat() {}

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public Integer getPageOrder() {
        return pageOrder;
    }

    public void setPageOrder(Integer pageOrder) {
        this.pageOrder = pageOrder;
    }

    public String getIsInputPresent() {
        return isInputPresent;
    }

    public void setIsInputPresent(String isInputPresent) {
        this.isInputPresent = isInputPresent;
    }

    public InputStat getInputStats() {
        return inputStats;
    }

    public void setInputStats(InputStat inputStats) {
        this.inputStats = inputStats;
    }

}