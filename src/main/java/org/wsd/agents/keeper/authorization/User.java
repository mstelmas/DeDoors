package org.wsd.agents.keeper.authorization;

import java.util.Objects;

public class User {
    private static final String NO_AUTHORIZATION_GRANTED = "0";

    private final String email;

    private final String password;

    private final String authorizationLevel;

    public final String getAuthorizationLevel(String mail, String pass) {
        if (Objects.equals(email, mail)
                && Objects.equals(password, pass))
            return authorizationLevel;

        return NO_AUTHORIZATION_GRANTED;
    }

    public User(String email, String password, String authorizationLevel) {
        this.email = new String(email);
        this.password = new String(password);
        this.authorizationLevel = new String(authorizationLevel);
    }
}
