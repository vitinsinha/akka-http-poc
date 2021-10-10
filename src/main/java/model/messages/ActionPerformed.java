package model.messages;

import java.io.Serializable;

public class ActionPerformed implements Serializable {
    private final String description;

    public ActionPerformed(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
