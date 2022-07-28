package com.ratmirdudin.models;

import java.util.List;

public class FinanceYahoo extends WebSite {

    public FinanceYahoo(List<String> companyUrls, String companyNameSelector, String industrySelector) {
        super(companyUrls, companyNameSelector, industrySelector);
    }
}
