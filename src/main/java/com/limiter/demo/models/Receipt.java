package com.limiter.demo.models;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "receipt")
@Data
@NoArgsConstructor
public class Receipt implements Serializable {
    @Id
    @GeneratedValue
    private long ID;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "receipt_objects", joinColumns = @JoinColumn(name="receipt_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name="object_id", referencedColumnName = "id"))
    private List<Purchaseobject> purchasedObjects = new ArrayList<>();
    private Date date= new Date();
    private long user_id;
    @Column(nullable = true)
    private String user_name;
    @Column(nullable = true)
    private String phone_number;
    @Column(nullable = true)
    private String location;
    @Column(nullable = false)
    private Boolean delivered=false;
}
