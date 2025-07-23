package com.esclient.ratingservice.repository;

import com.esclient.ratingservice.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
