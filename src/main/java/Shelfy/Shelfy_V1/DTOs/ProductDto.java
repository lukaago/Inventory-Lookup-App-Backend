package Shelfy.Shelfy_V1.DTOs;

import Shelfy.Shelfy_V1.entities.Product;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String name,
    String brand,
    String unit,
    BigDecimal defaultPrice,
    String imageUrl,
    boolean recommended,
    boolean isActive)
    {
        public static ProductDto from(Product p) {
            return new ProductDto(
                    p.getId(), p.getName(), p.getBrand(), p.getUnit(), p.getDefaultPrice(), p.getImageUrl(), p.isRecommended(), p.isActive()
            );
        }
}
