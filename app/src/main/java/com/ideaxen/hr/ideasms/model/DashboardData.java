package com.ideaxen.hr.ideasms.model;

import com.google.gson.annotations.SerializedName;

public class DashboardData {
    @SerializedName("running")
    private Visit visit;
    private String today;
    private String assigned;
    private String canceled;
    private String completed;
    @SerializedName("missed_week")
    private String missedWeek;
    @SerializedName("missed_month")
    private String missedMonth;
    private String pending;
    private String adjourned;

    public DashboardData(Visit visit, String today, String assigned, String canceled, String completed, String missedWeek, String missedMonth, String pending, String adjourned) {
        this.visit = visit;
        this.today = today;
        this.assigned = assigned;
        this.canceled = canceled;
        this.completed = completed;
        this.missedWeek = missedWeek;
        this.missedMonth = missedMonth;
        this.pending = pending;
        this.adjourned = adjourned;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getAssigned() {
        return assigned;
    }

    public void setAssigned(String assigned) {
        this.assigned = assigned;
    }

    public String getCanceled() {
        return canceled;
    }

    public void setCanceled(String canceled) {
        this.canceled = canceled;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getMissedWeek() {
        return missedWeek;
    }

    public void setMissedWeek(String missedWeek) {
        this.missedWeek = missedWeek;
    }

    public String getMissedMonth() {
        return missedMonth;
    }

    public void setMissedMonth(String missedMonth) {
        this.missedMonth = missedMonth;
    }

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }

    public String getAdjourned() {
        return adjourned;
    }

    public void setAdjourned(String adjourned) {
        this.adjourned = adjourned;
    }

    @Override
    public String toString() {
        return "DashboardData{" +
                "visit=" + visit +
                ", today='" + today + '\'' +
                ", assigned='" + assigned + '\'' +
                ", canceled='" + canceled + '\'' +
                ", completed='" + completed + '\'' +
                ", missedWeek='" + missedWeek + '\'' +
                ", missedMonth='" + missedMonth + '\'' +
                ", pending='" + pending + '\'' +
                ", adjourned='" + adjourned + '\'' +
                '}';
    }
}
