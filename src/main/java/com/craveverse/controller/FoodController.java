package com.craveverse.controller;

import com.craveverse.entity.Food;
import com.craveverse.repository.FoodRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodRepository foodRepository;

    public FoodController(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @GetMapping
    public List<Food> getAllFoods() {
        return foodRepository.findByAvailableTrue();
    }

    @GetMapping("/{id}")
    public Food getFoodById(@PathVariable Long id) {
        return foodRepository.findByIdAndAvailableTrue(id)
                .orElse(null);
    }

    @PostMapping
    public Food addFood(
            @RequestBody Food food,
            HttpSession session) {

        checkAdmin(session);
        return foodRepository.save(food);
    }

    @DeleteMapping("/{id}")
    public void deleteFood(
            @PathVariable Long id,
            HttpSession session) {

        checkAdmin(session);

        if (!foodRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Food item not found");
        }

        foodRepository.deleteById(id);
    }

    private void checkAdmin(HttpSession session) {
        if (session == null ||
                !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only an admin can manage food items");
        }
    }
}