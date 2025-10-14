package dataaccess;
import model.*;


public interface DataAccess {
    void clear();
    void addUser(UserData user);
    UserData getUser(String username);

}
