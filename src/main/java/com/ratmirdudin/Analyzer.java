package com.ratmirdudin;

import com.ratmirdudin.models.Company;
import com.ratmirdudin.models.WebSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Analyzer {

    private final List<String> keyWords = new ArrayList<>(List.of("the", "group", "company", "companies", "corporation", "inc", "corp", "co"));

    private String mapToIndustryFormat(String str) {
        return Arrays.stream(str.split("\\s++")).map(item -> item.substring(0, 1).toUpperCase() + item.substring(1)).collect(Collectors.joining(" "));
    }

    private String union(String s1, String s2) {
        s1 = s1.trim().replaceAll("\\s++", " ").toLowerCase();
        s2 = s2.trim().replaceAll("\\s++", " ").toLowerCase();

        if (s1.length() < s2.length()) {
            String tmp = s1;
            s1 = s2;
            s2 = tmp;
        }

        if (s1.isBlank() || s2.isBlank()) {
            return mapToIndustryFormat(s1.trim() + s2.trim());
        } else if (s1.contains(s2)) {
            return mapToIndustryFormat(s1);
        }

        List<String> s1Words = Arrays.stream(s1.split("\\s&\\s")).collect(Collectors.toList());
        List<String> s2Words = Arrays.stream(s2.split("\\s&\\s")).collect(Collectors.toList());

        List<String> concatListOFIndustries = Stream.concat(s1Words.stream(), s2Words.stream()).collect(Collectors.toList());

        for (int i = 0; i < concatListOFIndustries.size(); i++) {
            for (int j = 0; j < concatListOFIndustries.size(); j++) {
                if (i == j || concatListOFIndustries.get(i).isBlank() || concatListOFIndustries.get(j).isBlank()) {
                    continue;
                }
                String bigger = concatListOFIndustries.get(i);
                String smaller = concatListOFIndustries.get(j);
                if (bigger.length() < smaller.length()) {
                    String tmp = bigger;
                    bigger = smaller;
                    smaller = tmp;
                }
                if (bigger.contains(smaller)) {
                    concatListOFIndustries.set(concatListOFIndustries.indexOf(smaller), "");
                }
            }
        }
        String join = concatListOFIndustries.stream().filter(item -> !item.isBlank()).collect(Collectors.joining(" & "));
        return mapToIndustryFormat(join);
    }

    private List<Company> getCompanyNamesFromWebSite(WebSite webSite) throws IOException {
        List<Company> companyList = new ArrayList<>();
        for (String companyUrl : webSite.getCompanyUrls()) {
            String name = "";
            String industry = "";
            Document document = getDocument(companyUrl);
            Elements names = document.select(webSite.getNameSelector());
            if (names.size() == 1) {
                name = names.get(0).text();
            }

            Elements industries = document.select(webSite.getIndustrySelector());
            if (industries.size() == 1) {
                industry = industries.get(0).text();
            } else if (industries.size() == 3) {
                industry = industries.get(1).text();
            }
            companyList.add(new Company(name, industry));
        }
        return companyList;
    }

    private Document getDocument(String companyUrl) throws IOException {
        return Jsoup.connect(companyUrl)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201")
                .referrer("http://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .execute()
                .parse();
    }

    private boolean isMatching(String s1, String s2) {
        String sentence1 = new String(s1);
        String sentence2 = new String(s2);
        if (sentence1.length() < sentence2.length()) {
            String tmp = sentence1;
            sentence1 = sentence2;
            sentence2 = tmp;
        }
        return sentence1.contains(sentence2);
    }

    private List<String> getListWIthExcludingKeyWords(String str) {
        return Arrays.stream(str.split("\\s")).filter(word -> !this.keyWords.contains(word)).collect(Collectors.toList());
    }

    private String normalizeString(String str) {
        return str.trim().replaceAll("-", "")
                .replaceAll("\\.", "")
                .replaceAll(",", "")
                .replaceAll("\\s++", " ")
                .toLowerCase();
    }

    private boolean isMatchingByWords(String first, String second) {
        for (var word1 : getListWIthExcludingKeyWords(normalizeString(first))) {
            for (var word2 : getListWIthExcludingKeyWords(normalizeString(second))) {
                if (isMatching(word1, word2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> startAnalyze(WebSite yahoo, WebSite indeed) {
        try {

            List<Company> yahooCompanies = getCompanyNamesFromWebSite(yahoo);
            List<Company> indeedCompanies = getCompanyNamesFromWebSite(indeed);

            List<String> fuzzyStringsInfo = new ArrayList<>();
            for (var yahooCompany : yahooCompanies) {
                Company fuzzyIndeedCompany = null;
                for (var indeedCompany : indeedCompanies) {
                    if (isMatching(normalizeString(yahooCompany.getName()), normalizeString(indeedCompany.getName()))) {
                        fuzzyIndeedCompany = indeedCompany;
                        break;
                    } else if (isMatchingByWords(yahooCompany.getName(), indeedCompany.getName())) {
                        fuzzyIndeedCompany = indeedCompany;
                    }
                }
                if (fuzzyIndeedCompany != null) {
                    String unionIndustries = union(yahooCompany.getIndustry(), fuzzyIndeedCompany.getIndustry());
                    fuzzyStringsInfo.add("\"" + yahooCompany.getName() + "\"" + " and " + "\"" + fuzzyIndeedCompany.getName() + "\"" + " with Industry: " + "\"" + unionIndustries + "\"");
                }
            }
            return fuzzyStringsInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}