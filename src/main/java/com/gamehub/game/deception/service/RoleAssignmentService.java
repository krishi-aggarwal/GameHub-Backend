package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.exception.InvalidRoleConfigurationException;
import com.gamehub.game.exception.RoleAllocationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleAssignmentService {


    public List<DeceptionRole> calculateRoles(List<GamePlayer> gamePlayerList, DeceptionGameConfig deceptionGameConfig){
        List<DeceptionRole> deceptionRoleList = new ArrayList<>();

        Map<DeceptionRole,Integer> rolePool = new HashMap<>();

        int totalPlayers = gamePlayerList.size();
        Set<DeceptionRole> enabledRoles = deceptionGameConfig.getEnabledRoles();
        int predatorCount = 0 , doctorCount = 0 , detectiveCount = 0 , innocentCount = 0;
        if(totalPlayers <= 5){
            predatorCount = 1;
        } else if (totalPlayers <= 8)  {
            predatorCount = 2;
        }
        else if(totalPlayers <= 11){
            predatorCount = 3;
        }
        else{
            predatorCount = 4 ;
        }

        if(enabledRoles.contains(DeceptionRole.DOCTOR)){
            doctorCount++;
            rolePool.put(DeceptionRole.DOCTOR,doctorCount);
        }

        if(enabledRoles.contains(DeceptionRole.DETECTIVE)){
            detectiveCount++;
            rolePool.put(DeceptionRole.DETECTIVE,detectiveCount);
        }

        innocentCount = totalPlayers - (predatorCount + doctorCount + detectiveCount);

        rolePool.put(DeceptionRole.INNOCENT,innocentCount);
        rolePool.put(DeceptionRole.PREDATOR,predatorCount);

        for(Map.Entry<DeceptionRole,Integer> entry : rolePool.entrySet()){
            for(int j=0 ; j< entry.getValue();j++){
                deceptionRoleList.add(entry.getKey());
            }
        }

        if(totalPlayers != deceptionRoleList.size()){
            throw new InvalidRoleConfigurationException("RoleConfiguration : Something went wrong");
        }

        //random
        Collections.shuffle(deceptionRoleList);

        return deceptionRoleList;
    }

    public void allocateRoles(List<DeceptionRole> deceptionRoleList , List<GamePlayer> gamePlayerList){
        if(deceptionRoleList.size() != gamePlayerList.size()){
            throw new RoleAllocationException("Allocate Roles : Something went wrong");
        }

        for(int i=0 ; i<deceptionRoleList.size() ; i++){
            gamePlayerList.get(i).setRole(deceptionRoleList.get(i));
        }
    }
}
