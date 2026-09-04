
        package com.gamehub.game.deception.domain;

import java.util.HashSet;
import java.util.Set;

public class DeceptionGameConfig {

    private Set<DeceptionRole> enabledRoles =
            new HashSet<>(
                    Set.of(
                            DeceptionRole.PREDATOR,
                            DeceptionRole.INNOCENT
                    )
            );

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
        return enabledRoles;
    }
}

