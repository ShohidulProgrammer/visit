package com.ideaxen.hr.ideasms.model;

public class DataParam {
    private String visitId;
    private String visitStatus;
    private String msg1;
    private String msg2;
    private String msg3;
    private String msg4;

    public DataParam() {
    }

    public DataParam(String visitId, String visitStatus, String msg1, String msg2, String msg3, String msg4) {
        this.visitId = visitId;
        this.visitStatus = visitStatus;
        this.msg1 = msg1;
        this.msg2 = msg2;
        this.msg3 = msg3;
        this.msg4 = msg4;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    public String getMsg1() {
        return msg1;
    }

    public void setMsg1(String msg1) {
        this.msg1 = msg1;
    }

    public String getMsg2() {
        return msg2;
    }

    public void setMsg2(String msg2) {
        this.msg2 = msg2;
    }

    public String getMsg3() {
        return msg3;
    }

    public void setMsg3(String msg3) {
        this.msg3 = msg3;
    }

    public String getMsg4() {
        return msg4;
    }

    public void setMsg4(String msg4) {
        this.msg4 = msg4;
    }

}
