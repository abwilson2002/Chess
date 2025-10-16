package dataaccess;

import chess.ChessGame;
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
    public UserData getUser(String auth, Integer filler) {
        for (AuthData authentication : authList){
            if (Objects.equals(authentication.authToken(), auth)) {
                return getUser(authentication.username());
            }
        }
        return null;
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
    public boolean alreadyLoggedIn(String username) {
        for (AuthData authentication : authList){
            if (Objects.equals(authentication.username(), username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AuthData getAuth(String username) {
        for (AuthData authentication : authList){
            if (Objects.equals(authentication.username(), username)) {
                return authentication;
            }
        }
        return null;
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
    public Double createGame(String gameName) {
        Double gameID = gameList.size() + 1.0;
        var newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        gameList.put(gameID.toString(), newGame);
        return gameID;
    }

    @Override
    public GameData getGame(Double gameID) {
        return gameList.get(gameID.toString());
    }

    @Override
    public void joinGame(String username, GameData game) {
        String gameID = game.gameID().toString();
        GameData previousGame = gameList.get(gameID);
        if (game.blackUsername() == null) {
            gameList.put(gameID, new GameData(game.gameID(), username, previousGame.blackUsername(), previousGame.gameName(), previousGame.game()));
        } else {
            gameList.put(gameID, new GameData(game.gameID(), previousGame.whiteUsername(), username, previousGame.gameName(), previousGame.game()));
        }
    }

    @Override
    public void clear() {
        userList.clear();
        gameList.clear();
        authList.clear();
    }

    private String generateAuth() {
        return UUID.randomUUID().toString();
    }
}
