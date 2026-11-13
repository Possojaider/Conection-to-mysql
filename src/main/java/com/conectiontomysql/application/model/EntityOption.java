package com.conectiontomysql.application.model;

public record EntityOption(Object value, String label) {
    @Override
    public String toString() {
        return label;
    }
}
