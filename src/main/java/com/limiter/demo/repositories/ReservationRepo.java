package com.limiter.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.limiter.demo.models.Reservation;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation,Long>{
    
}
