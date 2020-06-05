package com.ideaxen.hr.ideasms.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class User extends RealmObject implements Parcelable{
    private String username;
    @SerializedName("user_aid")
    private String id;
    @SerializedName("employee_aid")
    private String empId;
    private String name;
    private String email;
    private String mobile;
    private String address;
    @SerializedName("user_group_aid")
    private String userGroupId;
    private String token;

    public User() {
    }

    public User(String username, String id, String empId, String name, String email, String mobile, String address, String userGroupId, String token) {
        this.username = username;
        this.id = id;
        this.empId = empId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
        this.userGroupId = userGroupId;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", id='" + id + '\'' +
                ", empId='" + empId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    protected User(Parcel in) {
        username = in.readString();
        id = in.readString();
        empId = in.readString();
        name = in.readString();
        email = in.readString();
        mobile = in.readString();
        address = in.readString();
        userGroupId = in.readString();
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(id);
        dest.writeString(empId);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(mobile);
        dest.writeString(address);
        dest.writeString(userGroupId);
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
