package com.tad.asannet.models;

import androidx.annotation.NonNull;

/**
 * Created by tad on 01/05/19.
 */

public class UserId {

    public String userId;

    public <T extends UserId> T withId(@NonNull final String id){
        this.userId=id;
        return (T)this;
    }

}
