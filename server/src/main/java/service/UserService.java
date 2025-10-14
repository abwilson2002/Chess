package service;
import model.*;
import dataaccess.*;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) throws DataAccessException {
        var existingUser = dataAccess.getUser(user.username());
            if (existingUser != null) {
                throw new DataAccessException("User already exists");
            }
        dataAccess.addUser(user);
        return new RegisterResponse(user.username(), "hello");
    }

    public RegisterResponse login(UserData user) {
        return new RegisterResponse(user.username(), "hi there");
    }
}
