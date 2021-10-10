package model;

public class User {

    private final Long id;

    private final String name;

    public User(Long id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
