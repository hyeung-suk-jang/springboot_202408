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


import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository repository;

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


}
