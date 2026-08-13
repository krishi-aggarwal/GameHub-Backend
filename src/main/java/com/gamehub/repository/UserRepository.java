package com.gamehub.repository;

import com.gamehub.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}