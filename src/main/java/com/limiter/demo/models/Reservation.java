package com.limiter.demo.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name="reservation")
@Entity
@Data
@NoArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String eventType;
    @Column(nullable = false)
    private String localisation;
    @Column(nullable = false)
    private String description;
}
