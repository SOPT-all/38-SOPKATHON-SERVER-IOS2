package org.sopt.sopkathon.user.repository;

import org.sopt.sopkathon.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
