package dataaccess;
import model.*;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;


public interface DataAccess {
    void init() throws DataAccessException;
    void clear();
    AuthData addUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    AuthData getUser(String auth, Integer filler) throws DataAccessException;
    AuthData addAuth(String username) throws DataAccessException;
    boolean checkAuth(String auth) throws DataAccessException;
    AuthData getAuth(String username) throws DataAccessException;
    void deleteAuth(String auth) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    Double createGame(String gameName) throws DataAccessException;
    GameData getGame(Double gameID) throws DataAccessException;
    void joinGame(String username, GameData game) throws DataAccessException;
    Integer totalUsers();
    Integer totalAuths();
    Integer totalGames();
}
