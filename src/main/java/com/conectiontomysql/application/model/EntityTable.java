package com.conectiontomysql.application.model;

import java.util.List;

public record EntityTable(List<EntityField> fields, List<String> primaryKey, List<Object[]> rows) {
    public EntityTable {
        fields = List.copyOf(fields);
        primaryKey = List.copyOf(primaryKey);
        rows = List.copyOf(rows);
    }
}
