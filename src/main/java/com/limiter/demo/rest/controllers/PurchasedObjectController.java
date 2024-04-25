package com.limiter.demo.rest.controllers;

import com.limiter.demo.models.Product;
import com.limiter.demo.models.Purchaseobject;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.ProductRepository;
import com.limiter.demo.repositories.PurchaseObjectRepo;
import com.limiter.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
                    return new ResponseEntity<>(client.get().getPurchaseobjectList(), HttpStatus.OK);
                }
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("products/{date}/all")
    public Object getUserProductsPerDay(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date)
    {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> client = userRepository.findByUsername(auth.getName());
        if(client.isPresent())
        {
                if (client.get().getPurchaseobjectList().isEmpty()) {
                    return new ResponseEntity<>("NO ITEMS PURCHASED YET", HttpStatus.EXPECTATION_FAILED);
                }
                else {
                    List<Purchaseobject> objs = client.get().getPurchaseobjectList().stream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
                    .collect(Collectors.toList());
                    return new ResponseEntity<>(objs, HttpStatus.OK);
                }
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("products/today")
    public Object getUserProductsForToday()
    {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> client = userRepository.findByUsername(auth.getName());
        if(client.isPresent())
        {
                if (client.get().getPurchaseobjectList().isEmpty()) {
                    return new ResponseEntity<>("NO ITEMS PURCHASED YET", HttpStatus.OK);
                }
                else {
                    List<Purchaseobject> objs = client.get().getPurchaseobjectList().stream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(LocalDate.now()))
                    .collect(Collectors.toList());
                    return new ResponseEntity<>(objs, HttpStatus.OK);
                }
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("products/bought/{date}")
    public Object getAllPurchasedProducts(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<UserEntity> users = userRepository.findAll();
        List<List<Purchaseobject>> purchaseobjects = users.parallelStream().map(obj->obj.getPurchaseobjectList()).collect(Collectors.toList());

        List<Purchaseobject> simple = purchaseobjects.parallelStream().flatMap(List::stream).collect(Collectors.toList());

         List<Purchaseobject>  objs =
          simple.parallelStream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date)).
          collect(Collectors.toList());

        return objs;
    }
    



}
