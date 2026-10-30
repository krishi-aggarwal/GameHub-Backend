
        package com.gamehub.game.deception.domain;

import java.util.HashSet;
import java.util.Set;

public class DeceptionGameConfig {

    private final Set<DeceptionRole> enabledRoles =
            new HashSet<>();

    public DeceptionGameConfig(
            Set<DeceptionRole> selectedRoles
    ) {

        if (selectedRoles != null) {
            enabledRoles.addAll(
                    selectedRoles
            );
        }
    }

    public Set<DeceptionRole> getEnabledRoles() {
                return Set.copyOf(enabledRoles);
    }
}

