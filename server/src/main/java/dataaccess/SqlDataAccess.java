package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;



public class SqlDataAccess implements DataAccess {

    @Override
    public void init() throws DataAccessException {
        String userTableCreate = """
                CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) UNIQUE PRIMARY KEY,
                password TEXT NULL,
                email VARCHAR(100) NULL
                )
                """;

        String authTableCreate = """
                CREATE TABLE IF NOT EXISTS auths (
                username VARCHAR(50) NULL,
                authToken VARCHAR(255) UNIQUE PRIMARY KEY
                )
                """;

        String gameTableCreate = """
                CREATE TABLE IF NOT EXISTS games (
                gameID DOUBLE UNIQUE PRIMARY KEY,
                whiteUsername VARCHAR(50) NULL,
                blackUsername VARCHAR(50) NULL,
                gameName VARCHAR(255) NULL,
                game TEXT NULL
                )
                """;
        Map<Integer, String> tableCreates = new HashMap<>();
        tableCreates.put(0, userTableCreate);
        tableCreates.put(1, authTableCreate);
        tableCreates.put(2, gameTableCreate);
        for (int i = 0; i < 3; i++) {
            try (var conn = DatabaseManager.getConnection()) {
                try (var statement = conn.createStatement()) {
                    statement.executeUpdate(tableCreates.get(i));
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Failed to create table", ex);
            }
        }
    }



    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.createStatement()) {
                statement.executeUpdate("DELETE FROM users");
                statement.executeUpdate("DELETE FROM auths");
                statement.executeUpdate("DELETE FROM games");
            }
        }
        catch(Exception ex) {
            throw new DataAccessException("Error: Failed to clear tables");
        }
    }

    @Override
    public AuthData addUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?);")) {
                String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

                statement.setString(1, user.username());
                statement.setString(2, hashedPassword);
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
    public AuthData getUser(String auth, Integer filler) throws DataAccessException {
        String getTheUser = "SELECT L.username, R.authToken FROM users as L JOIN auths as R ON L.username = R.username WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(getTheUser)) {
                statement.setString(1, auth);

                var result = statement.executeQuery();
                if (result.next()) {
                    String usersUsername = result.getString("username");
                    return new AuthData(usersUsername, auth);
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
                statement.executeUpdate();

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
                statement.executeUpdate();
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT * FROM games")) {
                List<GameData> gameList = new ArrayList<>(List.of());
                var result = statement.executeQuery();
                while (result.next()) {
                    Double gameID = result.getDouble("gameID");
                    String whiteUsername = result.getString("whiteUsername");
                    String blackUsername = result.getString("blackUsername");
                    String gameName = result.getString("gameName");
                    String game = result.getString("game");
                    var serializer = new Gson();
                    var fullGame = serializer.fromJson(game, ChessGame.class);
                    GameData nextGame = new GameData(gameID, whiteUsername, blackUsername, gameName, fullGame);
                    gameList.add(nextGame);
                }
                return gameList;
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
    }

    @Override
    public Double createGame(String gameName) throws DataAccessException {
        double numberOfGames = totalGames() + 1;
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, NULL, NULL, ?, ?)")) {
                statement.setDouble(1, numberOfGames);
                statement.setString(2, gameName);
                var serializer = new Gson();
                var game = new ChessGame();
                var newGame = serializer.toJson(game);
                statement.setString(3, newGame);
                statement.executeUpdate();

                return numberOfGames;
            }
        }
        catch(Exception ex) {
            throw new DataAccessException("Error, could not add auth");
        }
    }

    @Override
    public GameData getGame(Double gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT * FROM games WHERE gameID = ?")) {
                statement.setDouble(1, gameID);
                var result = statement.executeQuery();
                if (result.next()) {
                    String whiteUsername = result.getString("whiteUsername");
                    String blackUsername = result.getString("blackUsername");
                    String gameName = result.getString("gameName");
                    String game = result.getString("game");
                    var serializer = new Gson();
                    var fullGame = serializer.fromJson(game, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, fullGame);
                }
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
        return null;
    }

    @Override
    public void joinGame(String username, GameData game) throws DataAccessException {
        String chosenSide;
        if (game.whiteUsername() != null) {
            chosenSide = "whiteUsername";
        } else {
            chosenSide = "blackUsername";
        }
        String query = String.format("UPDATE games SET %s = ? WHERE gameID = ?", chosenSide);
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setDouble(2, game.gameID());
                var result = statement.executeUpdate();
            }
        }
        catch (Exception ex) {
            throw new DataAccessException ("Error, not authorized", ex);
        }
    }

    @Override
    public Integer totalUsers() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT COUNT(username) as count FROM users")) {
                var result = statement.executeQuery();
                if (result.next()) {
                    return result.getInt("count");
                }
            }
        }
        catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    @Override
    public Integer totalAuths() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT COUNT(username) as count FROM auths")) {
                var result = statement.executeQuery();
                if (result.next()) {
                    return result.getInt("count");
                }
            }
        }
        catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    @Override
    public Integer totalGames() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT COUNT(gameID) as count FROM games")) {
                var result = statement.executeQuery();
                if (result.next()) {
                    return result.getInt("count");
                }
            }
        }
        catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    private String generateAuth() {
        return UUID.randomUUID().toString();
    }

}
