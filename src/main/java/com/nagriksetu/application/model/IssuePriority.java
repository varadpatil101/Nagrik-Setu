package com.nagriksetu.application.model;

public enum IssuePriority {
    LOW("Low"), MEDIUM("Medium"), HIGH("High");
    private final String value;
    IssuePriority(String value) { this.value = value; }
    public String value() { return value; }
}
