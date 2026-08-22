package com.gamehub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 30)
    private String displayName;

    @Size(max = 500)
    private String avatarUrl;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UpdateProfileRequest(){

    }

    public UpdateProfileRequest(String displayName , String avatarUrl){
        this.displayName=displayName;
        this.avatarUrl=avatarUrl;
    }


}
