package com.craveverse.controller;

import com.craveverse.entity.Restaurant;
import com.craveverse.entity.Food;
import com.craveverse.repository.RestaurantRepository;
import com.craveverse.repository.FoodRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "http://localhost:5173")
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