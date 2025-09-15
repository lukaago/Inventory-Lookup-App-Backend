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

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addProduct(Product product) {

        productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getRecommendedProducts() {
        return productRepository.findByRecommendedTrue();
    }

    private static String norm(String s) {
        return (s == null || s.isBlank()) ? null : s.toLowerCase(java.util.Locale.ROOT);
    }

    public Page<ProductDto> findFiltered(String name, String brand,
                                         BigDecimal priceMin, BigDecimal priceMax,
                                         Boolean recommended, Pageable pageable) {
        name  = norm(name);
        brand = (brand == null || brand.isBlank()) ? null : brand; // exact match for brand currently
        Page<Product> page = productRepository.findFiltered(name, brand, priceMin, priceMax, recommended, pageable);
        return page.map(ProductDto::from);
    }

    public ProductDto findProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductDto.from(product);
    }

    @Transactional
    public Product setRecommendation(Long id, boolean recommended) {
        Product p = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        p.setRecommended(recommended);
        return p; // managed entity; flush by TX end
    }

    public List<Product> findProductsByName(String name, String brand,
                                            BigDecimal priceMin, BigDecimal priceMax,
                                            Boolean recommended,
                                            org.springframework.data.domain.Sort sort) {
        return productRepository.findByNameContainingIgnoreCase(name, brand, priceMin, priceMax, recommended, sort);
    }

    public List<String> getAllUniqueBrands() {
        return productRepository.findAllUniqueBrands();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));


        product.setName(updatedProduct.getName());
        product.setActive(updatedProduct.isActive());
        product.setBrand(updatedProduct.getBrand());
        product.setLastUpdate(LocalDateTime.now());
        product.setUnit(updatedProduct.getUnit());
        product.setDefaultPrice(updatedProduct.getDefaultPrice());
        product.setImageUrl(updatedProduct.getImageUrl());
        productRepository.save(product);
    }

    public void removeProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }


}
