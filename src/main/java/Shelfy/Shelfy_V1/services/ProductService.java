package Shelfy.Shelfy_V1.services;

import Shelfy.Shelfy_V1.entities.Product;
import Shelfy.Shelfy_V1.repositories.ProductRepository;
import org.springframework.stereotype.Service;

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

    public List<Product> findProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
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
