package com.craveverse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Food {

    @ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "restaurant_id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
private Restaurant restaurant;


public Restaurant getRestaurant() {
    return restaurant;
}

public void setRestaurant(Restaurant restaurant) {
    this.restaurant = restaurant;
}

@Column(nullable = false)
private boolean available = true;

public boolean isAvailable() {
    return available;
}

public void setAvailable(boolean available) {
    this.available = available;
}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Food name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @Positive(message = "Price must be greater than zero")
    @Column(nullable = false)
    private double price;

    private String imageUrl;

    private String description;

    public Food() {
    }

    public Food(String name, String category, double price,
                String imageUrl, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}