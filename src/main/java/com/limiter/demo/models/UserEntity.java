package com.limiter.demo.models;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    private String NIC;
    @Nullable
    private String apiKey;
    private double salary;
    private Date dateOfBirth;
    @Nullable
    private String randomized;
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Delivery> deliveries;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "user")
    private List<Purchaseobject> purchaseobjectList;

    private Boolean active=true;

    @ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName = "id"))
    private List<Role> roles =  new ArrayList<>();

    @JsonManagedReference(value = "delivery-user")
    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}

