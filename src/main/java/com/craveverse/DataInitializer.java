package com.craveverse;

import com.craveverse.entity.Food;
import com.craveverse.repository.FoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadFoodData(FoodRepository foodRepository) {
        return args -> {
            if (foodRepository.count() == 0) {
                List<Food> foods = List.of(
                    new Food(
                        "Chicken Biryani",
                        "Indian",
                        220.00,
                        "https://images.unsplash.com/photo-1563379091339-03246963d96c",
                        "Aromatic basmati rice with flavorful chicken."
                    ),
                    new Food(
                        "Chicken Pizza",
                        "Italian",
                        280.00,
                        "https://images.unsplash.com/photo-1513104890138-7c749659a591",
                        "Chicken pizza with cheese and delicious toppings."
                    ),
                    new Food(
                        "Chicken Burger",
                        "Fast Food",
                        160.00,
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
                        "Juicy chicken burger with fresh vegetables."
                    ),
                    new Food(
                        "Paneer Butter Masala",
                        "Indian",
                        190.00,
                        "https://images.unsplash.com/photo-1631452180519-c014fe946bc7",
                        "Paneer cooked in a creamy tomato gravy."
                    ),
                    new Food(
                        "Veg Noodles",
                        "Chinese",
                        140.00,
                        "https://images.unsplash.com/photo-1585032226651-759b368d7246",
                        "Stir-fried noodles with fresh vegetables."
                    )
                );

                foodRepository.saveAll(foods);
                System.out.println("CraveVerse sample food items added!");
            }
        };
    }
}
