package dataaccess;
import model.*;


public interface DataAccess {
    void clear();
    AuthData addUser(UserData user);
    UserData getUser(String username);
    AuthData addAuth(String username);
    boolean checkAuth(AuthData auth);
    void deleteAuth(AuthData auth);
}
