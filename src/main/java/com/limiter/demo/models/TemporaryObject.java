package com.limiter.demo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="temporary_object")
@Data
@NoArgsConstructor
public class TemporaryObject {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @Lob
    @Column(nullable = true)
    private byte[] image;
    private double price;
    private String description;
    private Date addedDate;
    private int quantity;
    private Boolean Bought=false;

}
