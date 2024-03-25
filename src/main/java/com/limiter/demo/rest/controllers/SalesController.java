package com.limiter.demo.rest.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.limiter.demo.models.Final;
import com.limiter.demo.models.Purchaseobject;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.PurchaseObjectRepo;
import com.limiter.demo.repositories.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/v1/sales")
public class SalesController {
    
    @Autowired
    private PurchaseObjectRepo purchaseobjectrRepo;
    @Autowired
    private UserRepository userRepository;
   

    @GetMapping("getTotal")
    public Object getTotalSold()
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> user  = userRepository.findByUsername(username);
        if(user.isPresent())
        {
                    List<Purchaseobject> pList = purchaseobjectrRepo.findAll().stream().collect(Collectors.toList());
          double sum =   pList.stream().mapToDouble(obj->obj.getPrice() * obj.getQuantity()).sum();
                    return new ResponseEntity<>(sum,HttpStatus.OK);
        }
            return new ResponseEntity<>("Please Log in",HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("getTotal/{date}")
    public Object getTotalSoldForDateX(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) throws ParseException
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> user  = userRepository.findByUsername(username);
        if(user.isPresent())
        {
        
                List<Purchaseobject> objects = purchaseobjectrRepo.findAll().stream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .equals(date)).collect(Collectors.toList());
                System.out.println("variable date is "+date);
                double sum =  objects.stream().mapToDouble(obj->obj.getPrice()*obj.getQuantity()).sum();
                
                return new ResponseEntity<>(sum,HttpStatus.OK);
        }
            return new ResponseEntity<>("Please Log in",HttpStatus.UNAUTHORIZED);
    }

}
