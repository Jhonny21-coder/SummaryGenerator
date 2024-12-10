package com.example.application.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class WebsiteInfoService {

    public String getWebsiteTitle(String url) {
        try {
            // Fetch the HTML content of the website
            Document document = Jsoup.connect(url)
                        .followRedirects(false)
                        .get();

            // Extract the <title> tag's text
            return document.title();
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to fetch title";
        }
    }

    public String getDomainName(String url) {
        try {
            // Ensure the URL starts with http:// or https://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url; // Add https:// by default if missing
            }

            // Parse the URL and get the hostname
            URI uri = new URI(url);
            String hostname = uri.getHost();

            // Extract the domain name (e.g., "google" from "www.google.com")
            String[] domainParts = hostname.split("\\.");
            String domainName = domainParts.length > 1 ? domainParts[domainParts.length - 2] : hostname;

            // Capitalize the first letter of the domain name
            return capitalize(domainName);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL";
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
