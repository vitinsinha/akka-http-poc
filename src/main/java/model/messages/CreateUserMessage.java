package model.messages;

import model.User;

import java.io.Serializable;

public class CreateUserMessage implements Serializable {
    private final User user;

    public CreateUserMessage(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
