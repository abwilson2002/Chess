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
                throw new DataAccessException("Error: User already exists");
            }
        var newUser = dataAccess.addUser(user);
        return new RegisterResponse(newUser.username(), newUser.authToken());
    }

    public RegisterResponse login(UserData user) throws DataAccessException {
        var checkExisting = dataAccess.getUser(user.username());
        if (checkExisting == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (checkExisting != user) {
            throw new DataAccessException("Error: unauthorized");
        }
        var authentication = dataAccess.addAuth(user.username());
        return new RegisterResponse(authentication.username(), authentication.authToken());
    }

    public LogoutResponse logout(AuthData user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(user);
        return new LogoutResponse();
    }
}
