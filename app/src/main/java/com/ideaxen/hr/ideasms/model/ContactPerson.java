package com.ideaxen.hr.ideasms.model;

public class ContactPerson {

    private String customerName;
    private String contactName;
    private String contactDesignation;
    private String contactNumber;

    public ContactPerson() {
    }

    public ContactPerson(String customerName, String contactName, String contactDesignation, String contactNumber) {
        this.customerName = customerName;
        this.contactName = contactName;
        this.contactDesignation = contactDesignation;
        this.contactNumber = contactNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactDesignation() {
        return contactDesignation;
    }

    public void setContactDesignation(String contactDesignation) {
        this.contactDesignation = contactDesignation;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String toString() {
        return "ContactPerson{" +
                "customerName='" + customerName + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactDesignation='" + contactDesignation + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
