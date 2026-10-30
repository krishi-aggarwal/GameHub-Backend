package com.gamehub.config;


import com.gamehub.domain.game.Game;
import com.gamehub.domain.game.GameStatus;
import com.gamehub.domain.game.GameType;


import com.gamehub.repository.GameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameCatalogInitializer {

    @Bean
    public CommandLineRunner initializeDeceptionGame(
            GameRepository gameRepository
    ) {
        return args -> {
            String slug = "deception-around-us";

            if (!gameRepository.existsBySlug(slug)) {
                gameRepository.save(createActiveGame(
                    "Deception Around Us",
                    slug,
                    "A social deduction game of hidden roles and group decisions.",
                    4,
                    15
                ));
                }

                String fishpondSlug = "fishpond";
                if (!gameRepository.existsBySlug(fishpondSlug)) {
                gameRepository.save(createActiveGame(
                    "Fishpond",
                    fishpondSlug,
                    "Write anonymous messages, discuss them together, then reveal authors at the end.",
                    3,
                    12
                ));
            }
        };
    }

            private Game createActiveGame(
                String name,
                String slug,
                String description,
                int minPlayers,
                int maxPlayers
            ) {
            Game game = Game.create(
                name,
                slug,
                description,
                minPlayers,
                maxPlayers,
                GameType.PARTY,
                null
            );
            game.updateDetails(
                name,
                slug,
                description,
                minPlayers,
                maxPlayers,
                GameType.PARTY,
                null,
                GameStatus.ACTIVE
            );
            return game;
            }
}