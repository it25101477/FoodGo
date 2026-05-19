package com.foodgo.service;

import com.foodgo.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class ReviewService {

    private static final String FILE_NAME = "reviews.txt";

    @Autowired
    private FileStorageService fileStorage;

    private String filePath() { return fileStorage.resolve(FILE_NAME); }

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath());
        if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); }
    }

    public void addReview(Review review) throws IOException {
        if (review.getId() == null || review.getId().isEmpty())
            review.setId(String.valueOf(System.currentTimeMillis()));
        if (review.getCreatedAt() == 0) review.setCreatedAt(System.currentTimeMillis());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(review.toFileLine()); writer.newLine();
        }
    }

    public boolean deleteReview(String reviewId, String userId, boolean isAdmin) throws IOException {
        List<Review> reviews = getAllReviewsInternal();
        boolean found = false;
        Iterator<Review> it = reviews.iterator();
        while (it.hasNext()) {
            Review r = it.next();
            if (r.getId().equals(reviewId)) {
                if (isAdmin || r.getUserId().equals(userId)) {
                    it.remove(); found = true; break;
                }
            }
        }
        if (found) saveAll(reviews);
        return found;
    }

    public boolean toggleHide(String reviewId) throws IOException {
        List<Review> reviews = getAllReviewsInternal();
        for (Review r : reviews) {
            if (r.getId().equals(reviewId)) {
                r.setHidden(!r.isHidden());
                saveAll(reviews); return true;
            }
        }
        return false;
    }

    public List<Review> getReviewsByFoodId(String foodId) throws IOException {
        List<Review> result = new ArrayList<>();
        for (Review r : getAllReviewsInternal())
            if (r.getFoodId().equals(foodId) && !r.isHidden()) result.add(r);
        return result;
    }

    public List<Review> getReviewsByUserId(String userId) throws IOException {
        List<Review> result = new ArrayList<>();
        for (Review r : getAllReviewsInternal())
            if (r.getUserId().equals(userId)) result.add(r);
        return result;
    }

    public List<Review> getAllReviews() throws IOException {
        List<Review> reviews = getAllReviewsInternal();
        reviews.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return reviews;
    }

    private List<Review> getAllReviewsInternal() throws IOException {
        List<Review> reviews = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return reviews;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) reviews.add(Review.fromFileLine(line));
        }
        return reviews;
    }

    public double getAverageRating(String foodId) throws IOException {
        List<Review> reviews = getReviewsByFoodId(foodId);
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    public int getReviewCount(String foodId) throws IOException {
        return getReviewsByFoodId(foodId).size();
    }

    private void saveAll(List<Review> reviews) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (Review r : reviews) { writer.write(r.toFileLine()); writer.newLine(); }
        }
    }
}
