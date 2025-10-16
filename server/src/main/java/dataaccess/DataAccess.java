package dataaccess;
import model.*;

import java.util.List;


public interface DataAccess {
    void clear();
    AuthData addUser(UserData user);
    UserData getUser(String username);
    UserData getUser(String auth, Integer filler);
    AuthData addAuth(String username);
    boolean checkAuth(AuthData auth);
    boolean checkAuth(String auth);
    boolean alreadyLoggedIn(String username);
    AuthData getAuth(String username);
    void deleteAuth(String auth);
    List<GameData> listGames();
    Double createGame(String gameName);
    GameData getGame(Double gameID);
    void joinGame(String username, GameData game);
}
