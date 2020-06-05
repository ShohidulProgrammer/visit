package com.ideaxen.hr.ideasms.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class VisitReport extends RealmObject implements Parcelable{
    @PrimaryKey
    private String id;
    private String visitDate;
    private String startTime;
    private String startedFrom;
    private String afterAnotherVisit;
    private String startedFromOtherReason;
    private String outOfficeTime;
    private String adjournedTime;
    private String adjournReason;
    private String adjournInstructionFrom;
    private String adjournResumeDate;
    private String adjournResumeTime;
    private String inCustomerTime;
    private String outCustomerTime;
    private String cancelTime;
    private String cancelReason;
    private String rescheduleTime;
    private String rescheduleReason;
    private String rescheduledVisitingDate;
    private String finishedStatus;
    private String feedbackNote;
    private String endTime;
    private int visitStatus;

    public VisitReport() {
    }

    public VisitReport(String id, String visitDate, String startTime, String adjournResumeDate, String adjournResumeTime, String startedFrom, String afterAnotherVisit, String startedFromOtherReason, String outOfficeTime, String inCustomerTime, String outCustomerTime, String cancelTime, String cancelReason, String rescheduleTime, String rescheduleReason, String rescheduledVisitingDate, String adjournedTime, String adjournReason, String adjournInstructionFrom, String finishedStatus, String feedbackNote, String endTime, int visitStatus) {
        this.id = id;
        this.visitDate = visitDate;
        this.startTime = startTime;
        this.adjournResumeDate = adjournResumeDate;
        this.adjournResumeTime = adjournResumeTime;
        this.startedFrom = startedFrom;
        this.afterAnotherVisit = afterAnotherVisit;
        this.startedFromOtherReason = startedFromOtherReason;
        this.outOfficeTime = outOfficeTime;
        this.inCustomerTime = inCustomerTime;
        this.outCustomerTime = outCustomerTime;
        this.cancelTime = cancelTime;
        this.cancelReason = cancelReason;
        this.rescheduleTime = rescheduleTime;
        this.rescheduleReason = rescheduleReason;
        this.rescheduledVisitingDate = rescheduledVisitingDate;
        this.adjournedTime = adjournedTime;
        this.adjournReason = adjournReason;
        this.adjournInstructionFrom = adjournInstructionFrom;
        this.finishedStatus = finishedStatus;
        this.feedbackNote = feedbackNote;
        this.endTime = endTime;
        this.visitStatus = visitStatus;
    }

    protected VisitReport(Parcel in) {
        id = in.readString();
        visitDate = in.readString();
        startTime = in.readString();
        adjournResumeDate = in.readString();
        adjournResumeTime = in.readString();
        startedFrom = in.readString();
        afterAnotherVisit = in.readString();
        startedFromOtherReason = in.readString();
        outOfficeTime = in.readString();
        inCustomerTime = in.readString();
        outCustomerTime = in.readString();
        cancelTime = in.readString();
        cancelReason = in.readString();
        rescheduleTime = in.readString();
        rescheduleReason = in.readString();
        rescheduledVisitingDate = in.readString();
        adjournedTime = in.readString();
        adjournReason = in.readString();
        adjournInstructionFrom = in.readString();
        finishedStatus = in.readString();
        feedbackNote = in.readString();
        endTime = in.readString();
        visitStatus = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(visitDate);
        dest.writeString(startTime);
        dest.writeString(adjournResumeDate);
        dest.writeString(adjournResumeTime);
        dest.writeString(startedFrom);
        dest.writeString(afterAnotherVisit);
        dest.writeString(startedFromOtherReason);
        dest.writeString(outOfficeTime);
        dest.writeString(inCustomerTime);
        dest.writeString(outCustomerTime);
        dest.writeString(cancelTime);
        dest.writeString(cancelReason);
        dest.writeString(rescheduleTime);
        dest.writeString(rescheduleReason);
        dest.writeString(rescheduledVisitingDate);
        dest.writeString(adjournedTime);
        dest.writeString(adjournReason);
        dest.writeString(adjournInstructionFrom);
        dest.writeString(finishedStatus);
        dest.writeString(feedbackNote);
        dest.writeString(endTime);
        dest.writeInt(visitStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VisitReport> CREATOR = new Creator<VisitReport>() {
        @Override
        public VisitReport createFromParcel(Parcel in) {
            return new VisitReport(in);
        }

        @Override
        public VisitReport[] newArray(int size) {
            return new VisitReport[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAdjournResumeDate() {
        return adjournResumeDate;
    }

    public void setAdjournResumeDate(String adjournResumeDate) {
        this.adjournResumeDate = adjournResumeDate;
    }

    public String getAdjournResumeTime() {
        return adjournResumeTime;
    }

    public void setAdjournResumeTime(String adjournResumeTime) {
        this.adjournResumeTime = adjournResumeTime;
    }

    public String getStartedFrom() {
        return startedFrom;
    }

    public void setStartedFrom(String startedFrom) {
        this.startedFrom = startedFrom;
    }

    public String getAfterAnotherVisit() {
        return afterAnotherVisit;
    }

    public void setAfterAnotherVisit(String afterAnotherVisit) {
        this.afterAnotherVisit = afterAnotherVisit;
    }

    public String getStartedFromOtherReason() {
        return startedFromOtherReason;
    }

    public void setStartedFromOtherReason(String startedFromOtherReason) {
        this.startedFromOtherReason = startedFromOtherReason;
    }

    public String getOutOfficeTime() {
        return outOfficeTime;
    }

    public void setOutOfficeTime(String outOfficeTime) {
        this.outOfficeTime = outOfficeTime;
    }

    public String getInCustomerTime() {
        return inCustomerTime;
    }

    public void setInCustomerTime(String inCustomerTime) {
        this.inCustomerTime = inCustomerTime;
    }

    public String getOutCustomerTime() {
        return outCustomerTime;
    }

    public void setOutCustomerTime(String outCustomerTime) {
        this.outCustomerTime = outCustomerTime;
    }

    public String getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(String cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getRescheduleTime() {
        return rescheduleTime;
    }

    public void setRescheduleTime(String rescheduleTime) {
        this.rescheduleTime = rescheduleTime;
    }

    public String getRescheduleReason() {
        return rescheduleReason;
    }

    public void setRescheduleReason(String rescheduleReason) {
        this.rescheduleReason = rescheduleReason;
    }

    public String getRescheduledVisitingDate() {
        return rescheduledVisitingDate;
    }

    public void setRescheduledVisitingDate(String rescheduledVisitingDate) {
        this.rescheduledVisitingDate = rescheduledVisitingDate;
    }

    public String getAdjournedTime() {
        return adjournedTime;
    }

    public void setAdjournedTime(String adjournedTime) {
        this.adjournedTime = adjournedTime;
    }

    public String getAdjournReason() {
        return adjournReason;
    }

    public void setAdjournReason(String adjournReason) {
        this.adjournReason = adjournReason;
    }

    public String getAdjournInstructionFrom() {
        return adjournInstructionFrom;
    }

    public void setAdjournInstructionFrom(String adjournInstructionFrom) {
        this.adjournInstructionFrom = adjournInstructionFrom;
    }

    public String getFinishedStatus() {
        return finishedStatus;
    }

    public void setFinishedStatus(String finishedStatus) {
        this.finishedStatus = finishedStatus;
    }

    public String getFeedbackNote() {
        return feedbackNote;
    }

    public void setFeedbackNote(String feedbackNote) {
        this.feedbackNote = feedbackNote;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(int visitStatus) {
        this.visitStatus = visitStatus;
    }

    @Override
    public String toString() {
        return "VisitReport{" +
                "id='" + id + '\'' +
                ", visitDate='" + visitDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", startedFrom='" + startedFrom + '\'' +
                ", afterAnotherVisit='" + afterAnotherVisit + '\'' +
                ", startedFromOtherReason='" + startedFromOtherReason + '\'' +
                ", outOfficeTime='" + outOfficeTime + '\'' +
                ", adjournedTime='" + adjournedTime + '\'' +
                ", adjournReason='" + adjournReason + '\'' +
                ", adjournInstructionFrom='" + adjournInstructionFrom + '\'' +
                ", adjournResumeDate='" + adjournResumeDate + '\'' +
                ", adjournResumeTime='" + adjournResumeTime + '\'' +
                ", inCustomerTime='" + inCustomerTime + '\'' +
                ", outCustomerTime='" + outCustomerTime + '\'' +
                ", cancelTime='" + cancelTime + '\'' +
                ", cancelReason='" + cancelReason + '\'' +
                ", rescheduleTime='" + rescheduleTime + '\'' +
                ", rescheduleReason='" + rescheduleReason + '\'' +
                ", rescheduledVisitingDate='" + rescheduledVisitingDate + '\'' +
                ", finishedStatus='" + finishedStatus + '\'' +
                ", feedbackNote='" + feedbackNote + '\'' +
                ", endTime='" + endTime + '\'' +
                ", visitStatus=" + visitStatus +
                '}';
    }
}
