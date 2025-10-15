package Shelfy.Shelfy_V1.DTOs;

import Shelfy.Shelfy_V1.entities.Product;

import java.math.BigDecimal;

// Data Transfer Object (DTO) for transferring product data.
public record ProductDto(
    Long id,
    String name,
    String brand,
    String unit,
    BigDecimal defaultPrice,
    String imageUrl,
    boolean recommended,
    boolean active)
    {
        // Convert a Product entity to a ProductDto.
        public static ProductDto from(Product p) {
            return new ProductDto(
                    p.getId(), p.getName(), p.getBrand(), p.getUnit(), p.getDefaultPrice(), p.getImageUrl(), p.isRecommended(), p.isActive()
            );
        }
}
