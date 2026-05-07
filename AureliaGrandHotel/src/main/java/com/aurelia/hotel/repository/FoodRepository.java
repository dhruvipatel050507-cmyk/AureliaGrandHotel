package com.aurelia.hotel.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.aurelia.hotel.model.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByStatus(String status);
}