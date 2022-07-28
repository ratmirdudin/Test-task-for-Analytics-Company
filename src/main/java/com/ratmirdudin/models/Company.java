package com.ratmirdudin.models;

public class Company {
    private final String name;
    private final String industry;

    public Company(String name, String industry) {
        this.name = name;
        this.industry = industry;
    }

    public String getIndustry() {
        return industry;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "\"" + name + "\"(" + industry + ")";
    }
}
