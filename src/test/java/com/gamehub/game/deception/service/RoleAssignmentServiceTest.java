package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.exception.InvalidRoleConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleAssignmentServiceTest {

    private final RoleAssignmentService service = new RoleAssignmentService();

    @Test
    void assignsOnlyAutomaticRolesWhenNoOptionalRolesAreSelected() {
        List<DeceptionRole> roles = service.calculateRoles(
                players(4),
                new DeceptionGameConfig(Set.of())
        );

        assertEquals(1, count(roles, DeceptionRole.PREDATOR));
        assertEquals(3, count(roles, DeceptionRole.INNOCENT));
        assertEquals(0, count(roles, DeceptionRole.DOCTOR));
        assertEquals(0, count(roles, DeceptionRole.DETECTIVE));
    }

    @Test
    void includesOnlySelectedOptionalRoles() {
        List<DeceptionRole> roles = service.calculateRoles(
                players(6),
                new DeceptionGameConfig(Set.of(DeceptionRole.DETECTIVE))
        );

        assertEquals(1, count(roles, DeceptionRole.DETECTIVE));
        assertEquals(0, count(roles, DeceptionRole.DOCTOR));
        assertEquals(2, count(roles, DeceptionRole.PREDATOR));
    }

    @Test
    void rejectsAutomaticRolesInOptionalRoleSelection() {
        assertThrows(
                InvalidRoleConfigurationException.class,
                () -> service.calculateRoles(
                        players(4),
                        new DeceptionGameConfig(Set.of(DeceptionRole.PREDATOR))
                )
        );
    }

    private List<GamePlayer> players(int count) {
        List<GamePlayer> players = new ArrayList<>();
        for (long userId = 1; userId <= count; userId++) {
            players.add(new GamePlayer(
                    userId,
                    "player" + userId,
                    "Player " + userId,
                    null,
                    null,
                    true
            ));
        }
        return players;
    }

    private long count(List<DeceptionRole> roles, DeceptionRole role) {
        return roles.stream().filter(role::equals).count();
    }
}