package com.ideaxen.hr.ideasms.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class Login extends RealmObject implements Parcelable{
    private String status;
    private String message;
    private String username;
    private String token;
    private String loginType;
    private User user;

    public Login() {
    }

    public Login(String status, String message, String username, String token, String loginType, User user) {
        this.status = status;
        this.message = message;
        this.username = username;
        this.token = token;
        this.loginType = loginType;
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Login{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", token='" + token + '\'' +
                ", loginType='" + loginType + '\'' +
                ", user=" + user +
                '}';
    }

    protected Login(Parcel in) {
        status = in.readString();
        message = in.readString();
        username = in.readString();
        token = in.readString();
        loginType = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(message);
        dest.writeString(username);
        dest.writeString(token);
        dest.writeString(loginType);
        dest.writeParcelable(user, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Login> CREATOR = new Creator<Login>() {
        @Override
        public Login createFromParcel(Parcel in) {
            return new Login(in);
        }

        @Override
        public Login[] newArray(int size) {
            return new Login[size];
        }
    };
}
