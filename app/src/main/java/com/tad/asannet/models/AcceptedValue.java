package com.tad.asannet.models;
import androidx.annotation.NonNull;

/**
 * Created by tad on 01/05/19.
 */

public class AcceptedValue {

    public boolean accepted;

    public <T extends AcceptedValue> T withAccepted(@NonNull final boolean id) {
        this.accepted = id;
        return (T) this;
    }

}
