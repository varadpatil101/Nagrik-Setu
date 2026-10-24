package com.nagriksetu.application.model;

public enum IssueStatus {
    REPORTED("Reported"), IN_PROGRESS("In progress"), RESOLVED("Resolved");
    private final String value;
    IssueStatus(String value) { this.value = value; }
    public String value() { return value; }
}
