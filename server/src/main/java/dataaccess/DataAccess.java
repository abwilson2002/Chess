package dataaccess;
import model.*;

import java.util.List;


public interface DataAccess {
    void clear();
    AuthData addUser(UserData user);
    UserData getUser(String username);
    AuthData addAuth(String username);
    boolean checkAuth(AuthData auth);
    boolean checkAuth(String auth);
    void deleteAuth(String auth);
    List<GameData> listGames();
}
