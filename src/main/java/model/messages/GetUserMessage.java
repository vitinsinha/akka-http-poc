package model.messages;

import java.io.Serializable;

public class GetUserMessage implements Serializable {
    private final Long userId;

    public GetUserMessage(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
