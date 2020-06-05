package com.ideaxen.hr.ideasms.model;

public class InstantVisit {
    private String customerId;
    private String customerName;
    private String reportingId;
    private String reportingName;
    private String reason;
    private String from;
    private String afterAnother;

    public InstantVisit(String customerId, String customerName, String reportingId, String reportingName, String reason, String from, String afterAnother) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.reportingId = reportingId;
        this.reportingName = reportingName;
        this.reason = reason;
        this.from = from;
        this.afterAnother = afterAnother;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getReportingId() {
        return reportingId;
    }

    public void setReportingId(String reportingId) {
        this.reportingId = reportingId;
    }

    public String getReportingName() {
        return reportingName;
    }

    public void setReportingName(String reportingName) {
        this.reportingName = reportingName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAfterAnother() {
        return afterAnother;
    }

    public void setAfterAnother(String afterAnother) {
        this.afterAnother = afterAnother;
    }

    @Override
    public String toString() {
        return "InstantVisit{" +
                "customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", reportingId='" + reportingId + '\'' +
                ", reportingName='" + reportingName + '\'' +
                ", reason='" + reason + '\'' +
                ", from='" + from + '\'' +
                ", afterAnother='" + afterAnother + '\'' +
                '}';
    }
}
