package com.limiter.demo.rest.controllers;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limiter.demo.models.Reservation;
import com.limiter.demo.repositories.ReservationRepo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("api/public/reservation")
@CrossOrigin(origins = "*")
public class ReservationController {
    @Autowired
    ReservationRepo reservationRepo;

    @PostMapping("add")
    //Create a reservation
   public Object reservationCreate( @RequestBody Reservation reservation)
    {
        reservationRepo.save(reservation);
        return new ResponseEntity<>("Reservation Done", HttpStatus.OK);
    }

    @GetMapping("all")
    //Find all Reservations
    public Object reservationSee()
     {
         return new ResponseEntity<>(reservationRepo.findAll().parallelStream().collect(Collectors.toList()), HttpStatus.OK);
     }

}
