package com.packt.cardatabase.web;

import com.packt.cardatabase.domain.CarRepository;
import com.packt.cardatabase.domain.Owner;
import com.packt.cardatabase.domain.Product;
import com.packt.cardatabase.domain.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository repository;

    private static final String THUMBNAIL_DIR = "src/main/resources/static/thumbnails/";


    @GetMapping(value="/product/all")
    public Iterable<Product> getProducts(){
        return repository.findAll();
    }

    @GetMapping(value="/product/{id}")
    public Optional<Product> getProduct(@PathVariable String id){
        return repository.findById(id);
    }

// delete
    @DeleteMapping(value="/delete/product/{id}")
    public ResponseEntity<?>  deleteProduct(@PathVariable String id){
        try {
            //role check : admin / user
            // 현재 인증된 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // 사용자의 권한(Role) 확인
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                // Admin일 때만 삭제 허용
                repository.deleteById(id);
                return ResponseEntity.ok().build();  // Return 200 OK with no body
            } else {
                // ROLE_USER일 경우 삭제 차단
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this product");
            }

        } catch (Exception e) {
            // Handle any exceptions, such as if the product doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    // 상품 수정
    @PatchMapping(value="/edit/product/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product updatedProduct) {
        Optional<Product> existingProduct = repository.findById(id);

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            product.setExplanation(updatedProduct.getExplanation());
            repository.save(product);
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    @PatchMapping(value = "/edit/product/thumbnail/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadThumbnail(@PathVariable String id, @RequestParam("thumbnail") MultipartFile file) {
        Optional<Product> existingProduct = repository.findById(id);

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            // Store the file in the specified directory
            String filePath = storeFile(file);  // Store the file and get the relative path
            product.setThumbnail(filePath);
            repository.save(product);
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    // Store the file locally in the /resources/static/thumbnails directory
    private String storeFile(MultipartFile file) {
        try {
            // Create directory if it doesn't exist
            Path directory = Paths.get(THUMBNAIL_DIR);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Generate a unique file name (e.g., use the original file name or generate a UUID)
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = Paths.get(THUMBNAIL_DIR + uniqueFilename);

            // Save the file to the directory
            Files.write(filePath, file.getBytes());

            // Return the relative path for access from the web
            return "thumbnails/" + uniqueFilename + "?v=" + System.currentTimeMillis();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }


}
