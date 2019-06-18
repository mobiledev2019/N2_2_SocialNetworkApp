package com.tad.asannet.models;

import androidx.annotation.NonNull;

/**
 * Created by tad on 01/05/19.
 */

public class PostId {

    public String postId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.postId = id;
        return (T) this;
    }

}
