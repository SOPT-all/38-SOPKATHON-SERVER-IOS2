package org.sopt.sopkathon.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String nickname;

    protected User() {
    }

    private User(String nickname) {
        this.nickname = nickname;
    }

    public static User create(String nickname) {
        return new User(nickname);
    }

    public Long id() {
        return id;
    }

    public String nickname() {
        return nickname;
    }

    public void assignIdForTest(Long id) {
        this.id = id;
    }
}
