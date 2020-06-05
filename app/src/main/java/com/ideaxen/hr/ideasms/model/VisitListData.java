package com.ideaxen.hr.ideasms.model;

import java.util.List;

public class VisitListData {
    public String hasRunningVisit;
    public List<Visit> visitList;

    public VisitListData(String hasRunningVisit, List<Visit> visitList) {
        this.hasRunningVisit = hasRunningVisit;
        this.visitList = visitList;
    }

    public String getHasRunningVisit() {
        return hasRunningVisit;
    }

    public void setHasRunningVisit(String hasRunningVisit) {
        this.hasRunningVisit = hasRunningVisit;
    }

    public List<Visit> getVisitList() {
        return visitList;
    }

    public void setVisitList(List<Visit> visitList) {
        this.visitList = visitList;
    }
}
