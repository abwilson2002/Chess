package dataaccess;

import model.AuthData;
import java.util.UUID;

public class authData {



    public authData() {}


    void checkAuth() throws DataAccessException {}


    AuthData createAuth(String username) throws DataAccessException {
        String token = generateToken();



        return new AuthData(token, username);
    }

    void deleteAuth() throws DataAccessException{}

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    void deleteAllAuths() throws DataAccessException{}
}
