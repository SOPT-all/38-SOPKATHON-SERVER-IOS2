package org.sopt.sopkathon.spot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "spots")
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    protected Spot() {
    }

    private Spot(String name) {
        this.name = name;
    }

    public static Spot create(String name) {
        return new Spot(name);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
