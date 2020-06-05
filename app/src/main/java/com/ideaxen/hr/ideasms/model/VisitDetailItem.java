package com.ideaxen.hr.ideasms.model;

import io.realm.RealmObject;

public class VisitDetailItem extends RealmObject {
    private String id;
    private String colName;
    private String colDisplayName;
    private String value;

    public VisitDetailItem() {
    }

    public VisitDetailItem(String id, String colName, String colDisplayName, String value) {
        this.id = id;
        this.colName = colName;
        this.colDisplayName = colDisplayName;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getColName() {
        return colName;
    }

    public String getColDisplayName() {
        return colDisplayName;
    }

    public String getValue() {
        return value;
    }
}
