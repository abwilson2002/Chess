package dataaccess;

import model.*;

import java.util.*;

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
    public boolean checkAuth(String auth) {
        for (AuthData authentication : authList){
            if (Objects.equals(authentication.authToken(), auth)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteAuth(String auth) {
        for (AuthData authentication : authList){
            if (Objects.equals(authentication.authToken(), auth)) {
                authList.remove(authentication);
            }
        }
    }

    @Override
    public List<GameData> listGames() {
        List<GameData> gList = new ArrayList<>(List.of());
        for (HashMap.Entry<String, GameData> game : gameList.entrySet()) {
            gList.add(game.getValue());
        }
        return gList;
    }

    @Override
    public Integer createGame(String gameName) {
        Integer gameID = gameList.size() + 1;
        var newGame = new GameData(gameID, null, null, gameName);
        gameList.put(gameID.toString(), newGame);
        return gameID;
    }

    @Override
    public void clear() {
        userList.clear();
    }

    private String generateAuth() {
        return UUID.randomUUID().toString();
    }
}
