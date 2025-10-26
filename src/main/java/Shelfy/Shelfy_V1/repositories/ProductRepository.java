package Shelfy.Shelfy_V1.repositories;


import Shelfy.Shelfy_V1.entities.Product;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


// Repository interface for managing Product entities.
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by name containing a specified string (case-insensitive) with
    // optional filters for brand, price range, and recommendation status.
    @Query("""
  SELECT p FROM Product p
  WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE CONCAT('%', LOWER(CAST(:name AS string)), '%'))
    AND (:recommended IS NULL OR p.recommended = :recommended)
    AND (:brands IS NULL OR p.brand IN :brands)
    AND (:priceMin IS NULL OR p.defaultPrice >= :priceMin)
    AND (:priceMax IS NULL OR p.defaultPrice <= :priceMax)
""")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name,
                                                 @Param("brands") List<String> brands,
                                                 @Param("priceMin") BigDecimal priceMin,
                                                 @Param("priceMax") BigDecimal priceMax,
                                                 @Param("recommended") Boolean recommended,
                                                 org.springframework.data.domain.Sort sort);

    // Find all products that are marked as recommended.
    List<Product> findByRecommendedTrue();

    // Find products with multiple optional filters and pagination support.
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
            @Param("recommended") boolean recommended,
            Pageable pageable);

    // Retrieve a list of all unique product brands.
    @Query("SELECT DISTINCT p.brand FROM Product p")
    List<String> findAllUniqueBrands();
}
