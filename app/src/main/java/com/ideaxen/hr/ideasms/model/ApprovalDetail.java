package com.ideaxen.hr.ideasms.model;

public class ApprovalDetail {
    private String id;
    //    @SerializedName("visiteeType")
    private String visiteeType;
    private String visiteeName;
    private String address;
    private String visitingDate;

    public ApprovalDetail(String id, String visiteeType, String visiteeName, String address, String visitingDate) {
        this.id = id;
        this.visiteeType = visiteeType;
        this.visiteeName = visiteeName;
        this.address = address;
        this.visitingDate = visitingDate;
    }

    public ApprovalDetail() {
    }

    public String getId() {
        return id;
    }

    public String getVisiteeType() {
        return visiteeType;
    }

    public String getVisiteeName() {
        return visiteeName;
    }

    public String getAddress() {
        return address;
    }

    public String getVisitingDate() {
        return visitingDate;
    }
}
