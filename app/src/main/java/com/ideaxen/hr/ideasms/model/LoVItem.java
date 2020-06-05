package com.ideaxen.hr.ideasms.model;

import io.realm.RealmObject;

public class LoVItem extends RealmObject {
    private String id;
    private String lovName;
    private String key;
    private String value;

    public LoVItem() {
    }

    public LoVItem(String lovName, String value) {
        this.id = "";
        this.lovName = lovName;
        this.value = value;
    }

    public LoVItem(String id, String lovName, String key, String value) {
        this.id = id;
        this.lovName = lovName;
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLovName() {
        return lovName;
    }

    public void setLovName(String lovName) {
        this.lovName = lovName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LoVItem{" +
                "id='" + id + '\'' +
                ", lovName='" + lovName + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
