package com.gamehub.game.deception.domain;

import com.gamehub.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

//@Entity
//@Table(name = "game-player")


//runtime domain object

public class GamePlayer {

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
    private User user;

//    @Enumerated(EnumType.STRING)
    private DeceptionRole role;

    private boolean isAlive;



    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public DeceptionRole getRole() {
        return role;
    }

    public User getUser(){
        return user;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public GamePlayer(User user , DeceptionRole role , boolean isAlive){
        this.user=user;
        this.role=role;
        this.isAlive=isAlive;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void assignRole(DeceptionRole role){
        this.role = role;
    }


}

