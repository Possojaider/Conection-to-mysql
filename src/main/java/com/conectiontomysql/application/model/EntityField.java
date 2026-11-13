package com.conectiontomysql.application.model;

public record EntityField(String name, int jdbcType, String typeName, boolean nullable,
        boolean autoIncrement, int ordinal, String foreignKeyTable, String foreignKeyColumn) {
}
