package dataaccess;

import chess.ChessGame;
import model.*;
import java.sql.*;
import java.util.*;



public class SqlDataAccess implements DataAccess {

    @Override
    public void init() throws DataAccessException {
        String userTableCreate = """
                CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) DEFAULT NULL PRIMARY KEY,
                password VARCHAR(255) DEFAULT NULL,
                email VARCHAR(255) DEFAULT NULL
                );
                """;

        String authTableCreate = """
                CREATE TABLE IF NOT EXISTS auths (
                username VARCHAR(255) DEFAULT NULL PRIMARY KEY,
                authToken VARCHAR(255) DEFAULT NULL,
                );
                """;

        String gameTableCreate = """
                CREATE TABLE IF NOT EXISTS games (
                username VARCHAR(255) DEFAULT NULL PRIMARY KEY,
                password VARCHAR(255) DEFAULT NULL,
                email VARCHAR(255) DEFAULT NULL
                );
                """;
        Map<Integer, String> tableCreates = new HashMap<Integer, String>();
        tableCreates.put(0, userTableCreate);
        tableCreates.put(1, authTableCreate);
        tableCreates.put(2, gameTableCreate);
        for (int i = 0; i < 3; i++) {
            try (var conn = DatabaseManager.getConnection()) {
                try (var statement = conn.prepareStatement(tableCreates.get(i))) {
                    statement.executeUpdate();
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to create table", ex);
            }
        }
    }



    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("DROP users, auths, games")) {
                statement.executeUpdate();
            }
        }
        catch(Exception ex) {
            return;
        }
    }

    @Override
    public AuthData addUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?);")) {
                statement.setString(1, user.username());
                statement.setString(2, user.password());
                statement.setString(3, user.email());

                statement.executeUpdate();

                return addAuth(user.username());
            }
        }
        catch (Exception ex) {
            throw new DataAccessException("Failed to add user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String getTheUser = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(getTheUser)) {
                statement.setString(1, username);

                var result = statement.executeQuery();
                if (result.next()) {
                    String usersUsername = result.getString("username");
                    String usersPass = result.getString("password");
                    String usersEmail = result.getString("email");

                    return new UserData(usersUsername, usersPass, usersEmail);
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException("Error, couldn't get user", ex);
        }
        return null;
    }

    @Override
    public UserData getUser(String auth, Integer filler) throws DataAccessException {
        String usersUsername = "";
        String getTheUser = "SELECT username, authToken FROM auths WHERE auth = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(getTheUser)) {
                statement.setString(1, auth);

                var result = statement.executeQuery();
                if (result.next()) {
                    usersUsername = result.getString("username");
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException("Error, couldn't get user", ex);
        }
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT username, password, email FROM users WHERE username = ?")) {
                statement.setString(1, usersUsername);
                var result = statement.executeQuery();
                if (result.next()) {
                    String usersPass = result.getString("password");
                    String usersEmail = result.getString("email");
                    return new UserData(usersUsername, usersPass, usersEmail);
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException("Error, couldn't get user", ex);
        }
        return null;
    }

    @Override
    public AuthData addAuth(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO auths (username, authToken) VALUES (?, ?);")) {
                String auth = generateAuth();
                statement.setString(1, username);
                statement.setString(2, auth);
                statement.executeQuery();

                return new AuthData(username, auth);
            }
        }
        catch(Exception ex) {
            throw new DataAccessException("Error, could not add auth");
        }
    }

    @Override
    public boolean checkAuth(String auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT authToken FROM auths WHERE authToken = ?")) {
                statement.setString(1, auth);
                var result = statement.executeQuery();
                if (result.next()) {
                    return true;
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
        return false;
    }

    @Override
    public AuthData getAuth(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT authToken FROM auths WHERE username = ?")) {
                statement.setString(1, username);
                var result = statement.executeQuery();
                if (result.next()) {
                    String userA = result.getString("authToken");
                    return new AuthData(username, userA);
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
        return null;
    }

    @Override
    public void deleteAuth(String auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("DELETE FROM auths WHERE authToken = ?")) {
                statement.setString(1, auth);
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public Double createGame(String gameName) throws DataAccessException {
        return 0.0;
    }

    @Override
    public GameData getGame(Double gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void joinGame(String username, GameData game) throws DataAccessException {

    }

    @Override
    public Integer totalUsers() {
        return 0;
    }

    @Override
    public Integer totalAuths() {
        return 0;
    }

    @Override
    public Integer totalGames() {
        return 0;
    }

    private String generateAuth() {
        return UUID.randomUUID().toString();
    }

}
