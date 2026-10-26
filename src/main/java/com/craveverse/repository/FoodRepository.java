package com.craveverse.repository;

import java.util.List;
import java.util.Optional;
import com.craveverse.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

List<Food> findByAvailableTrue();

Optional<Food> findByIdAndAvailableTrue(Long id);
}