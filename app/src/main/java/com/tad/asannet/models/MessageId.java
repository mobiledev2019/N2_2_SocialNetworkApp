package com.tad.asannet.models;

import androidx.annotation.NonNull;

/**
 * Created by tad on 01/05/19.
 */

public class MessageId {

    public String msgId;

    public <T extends MessageId> T withId(@NonNull final String id) {
        this.msgId = id;
        return (T) this;
    }

}
