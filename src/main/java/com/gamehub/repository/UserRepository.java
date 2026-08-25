package com.gamehub.repository;

import com.gamehub.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    //register
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    //login
    Optional<User> findByUsername(String username);

    //admin
    boolean existsByRole(User.Role role);
}