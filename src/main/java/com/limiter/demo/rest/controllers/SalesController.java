package com.limiter.demo.rest.controllers;

import org.springframework.web.bind.annotation.RestController;
import com.limiter.demo.models.Purchaseobject;
import com.limiter.demo.models.Receipt;
import com.limiter.demo.models.Reservation;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.payment.ReceiptController;
import com.limiter.demo.repositories.PurchaseObjectRepo;
import com.limiter.demo.repositories.ReceiptRepository;
import com.limiter.demo.repositories.ReservationRepo;
import com.limiter.demo.repositories.UserRepository;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("api/v1/sales")
@CrossOrigin(origins = "*")
public class SalesController {
    
    @Autowired
    private PurchaseObjectRepo purchaseobjectrRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    ReceiptRepository receiptRepository;
    public static final Logger logger = LoggerFactory.getLogger(SalesController.class);

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
                logger.info("variable date is "+date);
                // System.out.println("variable date is "+date);
                double sum =  objects.stream().mapToDouble(obj->obj.getPrice()*obj.getQuantity()).sum();
                logger.info("sum is "+sum);
                return new ResponseEntity<>(sum,HttpStatus.OK);
        }
            return new ResponseEntity<>("Please Log in",HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("getTotal/today")
    public Object getTotalSoldForToday()
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> user  = userRepository.findByUsername(username);
        if(user.isPresent())
        {
                List<Purchaseobject> objects = purchaseobjectrRepo.findAll().stream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .equals(LocalDate.now())).collect(Collectors.toList());
               
                double sum =  objects.stream().mapToDouble(obj->obj.getPrice()*obj.getQuantity()).sum();
                logger.info("sum is "+sum);
                return new ResponseEntity<>(sum,HttpStatus.OK);
        }
            return new ResponseEntity<>("Please Log in",HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("getTotalCommands/today")
    public Object getTotalCommands() 
    {
        List<Purchaseobject> objects = purchaseobjectrRepo.findAll().stream().filter(obj->obj.getAddedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        .equals(LocalDate.now())).collect(Collectors.toList());
        long number = objects.stream().count();
        return new ResponseEntity<>(number,HttpStatus.OK);
    }

    @GetMapping("getTotalTraiteur")
    public Object getTotalTraiteur() 
    {
        List<Reservation> objects = reservationRepo.findAll().stream().collect(Collectors.toList());
        long number = objects.stream().count();
        return new ResponseEntity<>(number,HttpStatus.OK);
    }
    
    @GetMapping("getDelivered")
    public Object getTotalDelivered() 
    {
        List<Receipt> objects = receiptRepository.findAll().stream().filter(obj->obj.getDelivered() == true).collect(Collectors.toList());
        long number = objects.stream().count();
        return new ResponseEntity<>(number,HttpStatus.OK);
    }
    

}
