package com.ratmirdudin.models;

import java.util.List;

public abstract class WebSite {
    private final String nameSelector;
    private final String industrySelector;
    private final List<String> companyUrls;

    public WebSite(List<String> companyUrls, String nameSelector, String industrySelector) {
        this.nameSelector = nameSelector;
        this.companyUrls = companyUrls;
        this.industrySelector = industrySelector;
    }

    public String getNameSelector() {
        return nameSelector;
    }

    public List<String> getCompanyUrls() {
        return companyUrls;
    }

    public String getIndustrySelector() {
        return industrySelector;
    }
}
