package com.tad.asannet.models;

import androidx.annotation.NonNull;

/**
 * Created by tad on 01/05/19.
 */

public class CommentId {

    public String commentId;

    public <T extends CommentId> T withId(@NonNull final String id) {
        this.commentId = id;
        return (T) this;
    }

}
