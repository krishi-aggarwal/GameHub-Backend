package com.gamehub.repository;

import com.gamehub.domain.game.Game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findBySlug(String slug);
    boolean existsBySlug(String slug);

}
