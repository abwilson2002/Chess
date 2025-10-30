package dataaccess;
import model.*;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;


public interface DataAccess {
    void init();
    void clear();
    AuthData addUser(UserData user) throws DataAccessException, SQLException;
    UserData getUser(String username) throws DataAccessException, SQLException;
    UserData getUser(String auth, Integer filler);
    AuthData addAuth(String username);
    boolean checkAuth(AuthData auth);
    boolean checkAuth(String auth);
    AuthData getAuth(String username);
    void deleteAuth(String auth);
    List<GameData> listGames();
    Double createGame(String gameName);
    GameData getGame(Double gameID);
    void joinGame(String username, GameData game);
    Integer totalUsers();
    Integer totalAuths();
    Integer totalGames();
}
