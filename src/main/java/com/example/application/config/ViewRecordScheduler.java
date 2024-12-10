package com.example.application.config;

import com.example.application.service.ViewTrackerService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ViewRecordScheduler {

    private final ViewTrackerService viewTrackerService;

    public ViewRecordScheduler(ViewTrackerService viewTrackerService) {
        this.viewTrackerService = viewTrackerService;
    }

    // Schedule task to run daily at midnight
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Manila")
    public void createDailyRecord() {
        LocalDate today = LocalDate.now();

        // Check if today's record already exists
        int existingViews = viewTrackerService.getTodayViews();

        if (existingViews == 0) {
            // If no record exists, create a new one with 0 views
            viewTrackerService.updateDailyViews(0);
            System.out.println("New daily record created for date: " + today);
        } else {
            System.out.println("Record for date " + today + " already exists.");
        }
    }
}
