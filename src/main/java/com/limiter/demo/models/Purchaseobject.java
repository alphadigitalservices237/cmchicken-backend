package com.limiter.demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
@Entity
@Table(name="purchase_object")
@Data
@NoArgsConstructor
public class Purchaseobject {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @Lob
    private byte[] image;
    private double price;
    private String description;
    private Boolean bought=false;
    private Date addedDate;
    @ManyToOne
    @JoinColumn(name = "user_id",insertable = false,updatable = false)
    private UserEntity user;
    private long user_id;
    private int quantity;
}
