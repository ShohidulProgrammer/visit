package com.ideaxen.hr.ideasms.model;

public class LatLon {
    private String id;
    private String event;
    private String lat;
    private String lng;
    private String visId;
    private String empId;
    private String createdAt;

    public LatLon() {
    }

    public LatLon(String event, String lat, String lng, String visId, String empId) {
        this.event = event;
        this.lat = lat;
        this.lng = lng;
        this.visId = visId;
        this.empId = empId;
    }

    public LatLon(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLon(String id, String empId, String visId, String event, String lat, String lng, String createdAt) {
        this.id = id;
        this.empId = empId;
        this.visId = visId;
        this.event = event;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getVisId() {
        return visId;
    }

    public void setVisId(String visId) {
        this.visId = visId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
