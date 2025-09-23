package Shelfy.Shelfy_V1.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Brand name must not be blank")
    private String brand;

    @NotBlank(message = "Unit must not be blank")
    private String unit;

    @NotNull(message = "Price must not be blank")
    private BigDecimal defaultPrice;

    @NotBlank(message = "Url must not be blank")
    private String imageUrl;

    private boolean isActive;

    @Column(name = "is_recommended", nullable = false)
    private boolean recommended = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdate;

    public Product() {
        this.isActive = true;
        this.recommended = true;
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    public Product(String name, String brand, String unit, BigDecimal defaultPrice, String imageUrl, boolean recommended) {
        this.name = name;
        this.brand = brand;
        this.unit = unit;
        this.defaultPrice = defaultPrice;
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.recommended = recommended;
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean newRecommended) {
        recommended = newRecommended;
    }
}
