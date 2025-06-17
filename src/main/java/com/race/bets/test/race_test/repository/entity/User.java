package com.race.bets.test.race_test.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    private int balance;

    @Version
    private Long version;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Bet> bets = new ArrayList<>();

    public User(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }

}
