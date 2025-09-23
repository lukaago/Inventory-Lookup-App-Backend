package Shelfy.Shelfy_V1.controllers;


import Shelfy.Shelfy_V1.DTOs.ProductDto;
import Shelfy.Shelfy_V1.entities.Product;
import Shelfy.Shelfy_V1.services.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<List<Product>> addProduct(@RequestBody List<Product> products) {
        for (Product product : products) {
            productService.addProduct(product);
        }
        return new ResponseEntity<>(products, HttpStatus.CREATED);
    }

    @Deprecated
    @GetMapping
    public Page<ProductDto> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            Pageable pageable // supports ?page=0&size=20&sort=price,asc
    ) {
        return productService.findFiltered(name, brand, priceMin, priceMax, recommended, pageable);
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable long id) {
        Product product = productService.getProductById(id);
        return ProductDto.from(product);
    }

    // should only be used by administrators. customers should not have access to update and delete.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        productService.updateProduct(id, product);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public List<Product> getProductsByName(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            org.springframework.data.domain.Sort sort
    ) {
        return productService.findProductsByName(name, brand, priceMin, priceMax, recommended, sort);
    }


    @GetMapping("/brands")
    public List<String> getAllUniqueBrands() {
        return productService.getAllUniqueBrands();
    }

    @Deprecated
    @GetMapping("/product-{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @Deprecated
    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @Deprecated
    @GetMapping("/recommended")
    public List<Product> getRecommendedProducts() {
        return productService.getRecommendedProducts();
    }
}
