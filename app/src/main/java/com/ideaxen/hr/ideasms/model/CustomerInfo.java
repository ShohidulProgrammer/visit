package com.ideaxen.hr.ideasms.model;

public class CustomerInfo {
    private String id;
    private String customerName;
    private String customerAddress;

    public CustomerInfo(String id, String customerName) {
        this.id = id;
        this.customerName = customerName;
    }

    public CustomerInfo(String id, String customerName, String customerAddress) {
        this.id = id;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
                "id='" + id + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                '}';
    }
}
