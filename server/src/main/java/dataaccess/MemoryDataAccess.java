package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    private HashMap<String, UserData> userList = new HashMap<>();
    private HashMap<String, UserData> gameList = new HashMap<>();
    private HashMap<String, UserData> authList = new HashMap<>();

    @Override
    public void addUser(UserData user) {
        userList.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return userList.get(username);
    }

    @Override
    public void clear() {
        userList.clear();
    }
}
