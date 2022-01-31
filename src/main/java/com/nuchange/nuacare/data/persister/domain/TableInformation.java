package com.nuchange.nuacare.data.persister.domain;

import java.util.HashSet;

public class TableInformation {
    HashSet<String> columns;
    String tableName;

    public HashSet<String> getColumns() {
        return columns;
    }

    public void setColumns(HashSet<String> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
