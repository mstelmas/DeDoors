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
        addUser("wykladowca1@elka.pw.edu.pl", "tajne1", "Level0");
        addUser("wykladowca2@elka.pw.edu.pl", "tajne2", "Level1");
        addUser("wykladowca3@elka.pw.edu.pl", "tajne3", "Level2");
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
