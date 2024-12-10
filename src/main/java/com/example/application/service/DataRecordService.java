package com.example.application.service;

import com.example.application.data.History;
import com.example.application.data.AppUser;
import com.example.application.data.DataRecord;
import com.example.application.repository.DataRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import reactor.core.publisher.UnicastProcessor;

@Service
public class DataRecordService {

    private final DataRecordRepository dataRecordRepository;
    private final UnicastProcessor<DataRecord> publisher;

    public DataRecordService(DataRecordRepository dataRecordRepository, UnicastProcessor<DataRecord> publisher) {
        this.dataRecordRepository = dataRecordRepository;
        this.publisher = publisher;
    }

    public void sendRating(AppUser sender, int ratingCount, String title) {
    	DataRecord rating = new DataRecord();
    	rating.setSender(sender);
    	rating.setTitle(title);
    	rating.setRatingCount(ratingCount);
    	dataRecordRepository.save(rating);

    	publisher.onNext(rating);
    }

    public List<DataRecord> getRatingsByRatingCount(int ratingCount) {
    	return dataRecordRepository.findByRatingCount(ratingCount);
    }

    public List<DataRecord> getAllRatings() {
    	return dataRecordRepository.findAll();
    }

    public Map<Integer, Double> calculateRatingDistribution() {
        List<DataRecord> ratings = getAllRatings();

        int totalRatings = ratings.size();

        if (totalRatings == 0) {
            return Collections.emptyMap();  // Return an empty map if no ratings exist
        }

        // Initialize a map to store the count of each star rating (1 to 5)
        Map<Integer, Long> starCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int count = i;
            starCounts.put(i, ratings.stream().filter(rating -> rating.getRatingCount() == count).count());
        }

        // Create a map to store the percentage for each star rating
        Map<Integer, Double> starPercentages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            double percentage = ((double) starCounts.get(i) / totalRatings) * 100;
            starPercentages.put(i, percentage);
        }

        return starPercentages;
    }

    public String calculateOverallRatingPercentage() {
        List<DataRecord> ratings = getAllRatings();
        int totalRatings = ratings.size();
        int maxRatingValue = 5;

        if (totalRatings == 0) {
            return "No ratings"; // Avoid division by zero
        }

        int totalRatingScore = ratings.stream()
                                      .mapToInt(DataRecord::getRatingCount)
                                      .sum();

        double percentage = ((double) totalRatingScore / (totalRatings * maxRatingValue)) * 100;

        return totalRatings + " total ratings, with an overall average of " + Math.round(percentage) + "%";
    }
}
