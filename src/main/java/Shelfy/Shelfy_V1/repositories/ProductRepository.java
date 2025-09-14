package Shelfy.Shelfy_V1.repositories;


import Shelfy.Shelfy_V1.entities.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByRecommendedTrue();
    List<Product> findByNameContainingIgnoreCaseAndRecommendedTrue(String name);

    @Query("""
  SELECT p FROM Product p
  WHERE (:name IS NULL OR LOWER(p.name) LIKE CONCAT('%', :name, '%'))
    AND (:recommended IS NULL OR p.recommended = :recommended)
    AND (:brand IS NULL OR p.brand = :brand)
    AND (:priceMin IS NULL OR p.defaultPrice >= :priceMin)
    AND (:priceMax IS NULL OR p.defaultPrice <= :priceMax)
""")
    Page<Product> findFiltered(
            @Param("name") String name,
            @Param("brand") String brand,
            @Param("priceMin") BigDecimal priceMin,
            @Param("priceMax") BigDecimal priceMax,
            @Param("recommended") Boolean recommended,
            Pageable pageable);
}
