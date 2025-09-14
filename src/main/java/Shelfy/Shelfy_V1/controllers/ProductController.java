package Shelfy.Shelfy_V1.controllers;


import Shelfy.Shelfy_V1.DTOs.ProductDto;
import Shelfy.Shelfy_V1.entities.Product;
import Shelfy.Shelfy_V1.services.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public Page<ProductDto> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Boolean inStock,
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
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        productService.updateProduct(id, product);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Deprecated
    @GetMapping("/search")
    public List<Product> getProductsByName(@RequestParam String name) {
        return productService.findProductsByName(name);
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
