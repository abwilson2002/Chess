package service;
import model.*;
import dataaccess.*;

import java.util.Objects;

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
        if (user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        var newUser = dataAccess.addUser(user);
        return new RegisterResponse(newUser.username(), newUser.authToken());
    }

    public RegisterResponse login(UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        var checkExisting = dataAccess.getUser(user.username());
        if (checkExisting == null || !Objects.equals(checkExisting.password(), user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
        var authentication = dataAccess.addAuth(user.username());
        return new RegisterResponse(authentication.username(), authentication.authToken());
    }

    public LogoutResponse logout(String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        dataAccess.deleteAuth(user);
        return new LogoutResponse();
    }

    public ListResponse list(String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        return new ListResponse(dataAccess.listGames());
    }

    public CreateResponse create(String gameName, String user) throws DataAccessException {
        var checkExisting = dataAccess.checkAuth(user);
        if (!checkExisting) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null) {
            throw new DataAccessException("Error: bad request");
        }
        return new CreateResponse(dataAccess.createGame(gameName));
    }
}
