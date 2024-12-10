package com.example.application.service;

import com.example.application.data.ViewRecord;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ViewTrackerService {

    @PersistenceContext
    private EntityManager entityManager;

    // Add a new daily record
    @Transactional
    public void addDailyViewRecord(ViewRecord record) {
        entityManager.persist(record);
    }

    // Get total views for a specific day
    public int getViewsByDate(LocalDate date) {
        String query = "SELECT v.views FROM ViewRecord v WHERE v.date = :date";

	int result = 0;
	try {
    	    result = entityManager.createQuery(query, Integer.class)
            	.setParameter("date", date)
            	.getSingleResult();
	} catch (NoResultException e) {
	    // Handle case where no result is found
	    result = 0; // Default value for no results
	}
        return result;
    }

    // Update views for today
    @Transactional
    public void updateDailyViews(int newViews) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));

        // Check if today's record exists
        List<ViewRecord> todayRecord = entityManager.createQuery(
                "SELECT v FROM ViewRecord v WHERE v.date = :date", ViewRecord.class)
            .setParameter("date", today)
            .getResultList();

        if (todayRecord.isEmpty()) {
            // Create new record for today
            addDailyViewRecord(new ViewRecord(today, newViews));
        } else {
            // Update today's views
            ViewRecord record = todayRecord.get(0);
            record.setViews(record.getViews() + newViews);
            entityManager.merge(record);
        }
    }

    // Get yesterday's views
    public int getYesterdayViews() {
        return getViewsByDate(LocalDate.now(ZoneId.of("Asia/Manila")).minusDays(1));
    }

    // Get today's views
    public int getTodayViews() {
        return getViewsByDate(LocalDate.now(ZoneId.of("Asia/Manila")));
    }

    // Fetch data for each day in the past 7 days, including today
    public Map<LocalDate, Integer> getDailyViewsForPast12Days() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
        Map<LocalDate, Integer> dailyViews = new HashMap<>();

        // Loop through the last 12 days
        for (int i = 0; i < 12; i++) {
            LocalDate date = today.minusDays(i);
            int views = getViewsByDate(date); // Reuse existing method
            dailyViews.put(date, views);
        }

        return dailyViews;
    }

    // Fetch records for the past 12 days
    public List<ViewRecord> getRecordsForPast12Days() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
        LocalDate twelveDaysAgo = today.minusDays(12);

        String query = "SELECT v FROM ViewRecord v WHERE v.date BETWEEN :startDate AND :endDate ORDER BY v.date ASC";
        return entityManager.createQuery(query, ViewRecord.class)
                .setParameter("startDate", twelveDaysAgo)
                .setParameter("endDate", today)
                .getResultList();
    }
}
