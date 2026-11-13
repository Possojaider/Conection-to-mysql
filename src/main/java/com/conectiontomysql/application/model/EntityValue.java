package com.conectiontomysql.application.model;

public record EntityValue(String name, int jdbcType, Object value) {
}
