package Shelfy.Shelfy_V1.repositories;


import Shelfy.Shelfy_V1.entities.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
}
