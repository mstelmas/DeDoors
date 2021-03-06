package org.wsd.agents.keeper.authorization;

import java.util.HashMap;

// TODO this should be in ERES_KEEPER agent
// This class mock real ERES server
// in this implementation read only access is provided
public class ERESGate {

    private HashMap<String, User> users;

    private static final ERESGate instance = new ERESGate();

    public static ERESGate getInstance() {
        return instance;
    }

    private ERESGate() {
        users = new HashMap<>();

        // TODO consider authorizationLevels
        addUser("lecturer1@elka.pw.edu.pl", "password1", "Level1");
        addUser("lecturer2@elka.pw.edu.pl", "password2", "Level2");
        addUser("lecturer3@elka.pw.edu.pl", "password3", "Level3");
        addUser("itspecialist@tech.pw.edu.pl", "password4", "Level3");
    }

    private void addUser(String email, String password, String authorizationLevel) {
        User user = new User(email, password, authorizationLevel);
        users.put(email, user);
    }

    public String getAuthorizationLevel(String email, String password) {
        User user = users.get(email);
        String authorizationLevel = user.getAuthorizationLevel(email, password);
        return authorizationLevel;
    }
}
