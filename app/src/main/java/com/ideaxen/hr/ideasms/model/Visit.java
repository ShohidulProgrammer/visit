package com.ideaxen.hr.ideasms.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Visit extends RealmObject implements Parcelable{

    @PrimaryKey
    private String id;
    private String customer;
    private String address;
    @SerializedName("contact_person")
    private String contactPerson;
    @SerializedName("contact_number")
    private String contactNumber;
    @SerializedName("visit_date")
    private String visitDate;
    @SerializedName("visit_priority")
    private String visitPriority;
    @SerializedName("is_instant")
    private String isInstant;
    @SerializedName("visit_status")
    private String visitStatus;

    public Visit() {
    }

    public Visit(String id, String customer, String address, String contactPerson, String contactNumber, String visitDate, String visitPriority, String isInstant, String visitStatus) {
        this.id = id;
        this.customer = customer;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactNumber = contactNumber;
        this.visitDate = visitDate;
        this.visitPriority = visitPriority;
        this.isInstant = isInstant;
        this.visitStatus = visitStatus;
    }

    protected Visit(Parcel in) {
        id = in.readString();
        customer = in.readString();
        address = in.readString();
        contactPerson = in.readString();
        contactNumber = in.readString();
        visitDate = in.readString();
        visitPriority = in.readString();
        isInstant = in.readString();
        visitStatus = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(customer);
        dest.writeString(address);
        dest.writeString(contactPerson);
        dest.writeString(contactNumber);
        dest.writeString(visitDate);
        dest.writeString(visitPriority);
        dest.writeString(isInstant);
        dest.writeString(visitStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Visit> CREATOR = new Creator<Visit>() {
        @Override
        public Visit createFromParcel(Parcel in) {
            return new Visit(in);
        }

        @Override
        public Visit[] newArray(int size) {
            return new Visit[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getVisitPriority() {
        return visitPriority;
    }

    public void setVisitPriority(String visitPriority) {
        this.visitPriority = visitPriority;
    }

    public String getIsInstant() {
        return isInstant;
    }

    public void setIsInstant(String isInstant) {
        this.isInstant = isInstant;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id='" + id + '\'' +
                ", customer='" + customer + '\'' +
                ", address='" + address + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", visitDate='" + visitDate + '\'' +
                ", visitPriority='" + visitPriority + '\'' +
                ", isInstant='" + isInstant + '\'' +
                ", visitStatus='" + visitStatus + '\'' +
                '}';
    }
}
