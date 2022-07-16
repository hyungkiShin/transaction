package com.transaction.propagation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Member {

    @Id @GeneratedValue
    private Long Id;
    private String username;

    public Member() {

    }

    public Member(final String username) {
        this.username = username;
    }
}
