package com.ratmirdudin;

import com.ratmirdudin.models.FinanceYahoo;
import com.ratmirdudin.models.Indeed;
import com.ratmirdudin.models.WebSite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static List<String> readUrlsFromFile(String path) throws IOException {
        return Files.readAllLines(Paths.get(path))
                .stream()
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        List<String> yahooCompanyUrls = new ArrayList<>();
        List<String> indeedCompanyUrls = new ArrayList<>();
        try {
            yahooCompanyUrls = readUrlsFromFile("src\\main\\resources\\yahooUrls.txt");
            indeedCompanyUrls = readUrlsFromFile("src\\main\\resources\\indeedUrls.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Shuffle Data For Pure Experiment
        Collections.shuffle(yahooCompanyUrls);
        Collections.shuffle(indeedCompanyUrls);

        WebSite yahoo = new FinanceYahoo(
                yahooCompanyUrls,
                "div > h1",
                "p[class=D(ib) Va(t)] > span[class=Fw(600)]");
        WebSite indeed = new Indeed(
                indeedCompanyUrls,
                ".css-17x766f.e1wnkr790, .css-86gyd7.e1wnkr790",
                "li[data-testid=companyInfo-industry] > div.css-1w0iwyp.e1wnkr790");

        Analyzer analyzer = new Analyzer();
        List<String> data = analyzer.startAnalyze(yahoo, indeed);
        System.out.println("+--------------------------------------------------------------------------------------------------------------------------------------");
        data.forEach(item -> System.out.println("|" + item));
        System.out.println("+--------------------------------------------------------------------------------------------------------------------------------------");

        String dateTime = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yy")) + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH.mm.ss"));
        Files.createDirectories(Paths.get("src\\main\\resources\\results\\"));
        Files.write(Paths.get("src\\main\\resources\\results\\" + dateTime + ".txt"), data);
    }
}
