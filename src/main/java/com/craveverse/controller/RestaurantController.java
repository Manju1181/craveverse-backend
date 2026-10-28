package com.craveverse.controller;

import com.craveverse.entity.Food;
import com.craveverse.entity.Restaurant;
import com.craveverse.repository.FoodRepository;
import com.craveverse.repository.RestaurantRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;

    public RestaurantController(
            RestaurantRepository restaurantRepository,
            FoodRepository foodRepository) {
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
    }

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @GetMapping("/{id}/foods")
    public List<Food> getFoodsByRestaurant(@PathVariable Long id) {
        return foodRepository.findByRestaurant_IdAndAvailableTrue(id);
    }
}