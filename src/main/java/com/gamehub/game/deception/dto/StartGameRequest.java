package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.DeceptionRole;


import java.util.Set;

public class StartGameRequest {
    Set<DeceptionRole> selectedRoles;


    public StartGameRequest(){

    }

    public Set<DeceptionRole> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(Set<DeceptionRole> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }
}
