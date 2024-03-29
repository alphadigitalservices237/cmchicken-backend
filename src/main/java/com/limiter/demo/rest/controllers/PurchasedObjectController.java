package com.limiter.demo.rest.controllers;

import com.limiter.demo.models.Product;
import com.limiter.demo.models.Purchaseobject;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.ProductRepository;
import com.limiter.demo.repositories.PurchaseObjectRepo;
import com.limiter.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/v1/auth")
@CrossOrigin(origins = "*")
public class PurchasedObjectController {
    @Autowired
    private PurchaseObjectRepo purchaseObjectRepo;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("products/all")
    public Object getUserProducts()
    {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> client = userRepository.findByUsername(auth.getName());
        if(client.isPresent())
        {
                if (client.get().getPurchaseobjectList().isEmpty()) {
                    return new ResponseEntity<>("NO ITEMS PURCHASED YET", HttpStatus.EXPECTATION_FAILED);
                }
                else {
                    return new ResponseEntity<>(client.get().getPurchaseobjectList(), HttpStatus.FOUND);
                }
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN", HttpStatus.UNAUTHORIZED);
        }
    }



}
