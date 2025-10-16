package dataaccess;

import model.*;
import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;

public class MemoryDataAccess implements DataAccess {

    private HashMap<String, UserData> userList = new HashMap<>();
    private HashMap<String, GameData> gameList = new HashMap<>();
    private HashSet<AuthData> authList = new HashSet<>();

    @Override
    public AuthData addUser(UserData user) {
        userList.put(user.username(), user);
        String newAuth = generateAuth();
        var newAuthentication = new AuthData(user.username(), newAuth);
        authList.add(newAuthentication);
        return newAuthentication;
    }

    @Override
    public UserData getUser(String username) {
        return userList.get(username);
    }

    @Override
    public AuthData addAuth(String username) {
        AuthData newAuth = new AuthData(username, generateAuth());
        authList.add(newAuth);
        return newAuth;
    }

    @Override
    public boolean checkAuth(AuthData auth) {
        return authList.contains(auth);
    }

    @Override
    public void deleteAuth(AuthData auth) {
        authList.remove(auth);
    }

    @Override
    public void clear() {
        userList.clear();
    }

    private String generateAuth() {
        return UUID.randomUUID().toString();
    }
}
