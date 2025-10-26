package Shelfy.Shelfy_V1.services;

import Shelfy.Shelfy_V1.DTOs.ProductDto;
import Shelfy.Shelfy_V1.entities.Product;
import Shelfy.Shelfy_V1.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Service class for managing products.
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add a new product to the repository.
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    // Retrieve all products from the repository.
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Retrieve all recommended products from the repository.
    public List<Product> getRecommendedProducts() {
        return productRepository.findByRecommendedTrue();
    }

    // Normalize a string by converting it to lowercase or returning null if it's blank.
    private static String norm(String s) {
        return (s == null || s.isBlank()) ? null : s.toLowerCase(java.util.Locale.ROOT);
    }

    // Find products based on various filters and pagination.
    public Page<ProductDto> findFiltered(String name, String brand,
                                         BigDecimal priceMin, BigDecimal priceMax,
                                         boolean recommended, Pageable pageable) {
        name  = norm(name);
        brand = (brand == null || brand.isBlank()) ? null : brand; // exact match for brand currently
        Page<Product> page = productRepository.findFiltered(name, brand, priceMin, priceMax, recommended, pageable);
        return page.map(ProductDto::from);
    }

    // Find a product by its ID and return it as a ProductDto.
    public ProductDto findProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductDto.from(product);
    }

    // Set the recommendation status of a product.
    @Transactional
    public Product setRecommendation(Long id, boolean recommended) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        p.setRecommended(recommended);
        return p; // managed entity; flush by TX end
    }

    // Find products by name with various optional filters and sorting.
    public List<Product> findProductsByName(String name, List<String> brands,
                                            BigDecimal priceMin, BigDecimal priceMax,
                                            Boolean recommended,
                                            org.springframework.data.domain.Sort sort) {
        // Normalize the name parameter for case-insensitive search
        String normName = (name == null || name.isBlank()) ? null : name.trim();
        // Use null for empty brands so the JPQL (:brands IS NULL OR p.brand IN :brands) branch works
        List<String> normBrands = (brands == null || brands.isEmpty()) ? null : brands;
        return productRepository.findByNameContainingIgnoreCase(normName, normBrands, priceMin, priceMax, recommended, sort);
    }

    // Retrieve a list of all unique product brands.
    public List<String> getAllUniqueBrands() {
        return productRepository.findAllUniqueBrands();
    }

    // Get a product by its ID.
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // Update an existing product with new details.
    public Product updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));


        product.setName(updatedProduct.getName());
        product.setActive(updatedProduct.isActive());
        product.setBrand(updatedProduct.getBrand());
        product.setLastUpdate(LocalDateTime.now());
        product.setUnit(updatedProduct.getUnit());
        product.setDefaultPrice(updatedProduct.getDefaultPrice());
        product.setRecommended(updatedProduct.isRecommended());
        product.setImageUrl(updatedProduct.getImageUrl());
        return productRepository.save(product);
    }

    // Remove a product from the repository by its ID.
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }


}
