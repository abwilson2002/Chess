package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SqlDataAccess;
import model.*;
import org.junit.jupiter.api.*;
import passoff.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;

public class MyServiceTests {

    static UserData backgroundUser = new UserData("ghostUser", "casper", "shhh");
    static UserData baseUser = new UserData("user1", "pass1", "1");
    static UserData secondUser = new UserData("user2", "pass2", "2");
    static GameData firstGame = new GameData(0.0, null, null, "test", new ChessGame());
    static GameData secondGame = new GameData(1.0, null, null, "tester", new ChessGame());
    static JoinData firstJoin = new JoinData(0.0, "WHITE", "hello");
    static DataAccess dataAccess = new SqlDataAccess();
    static UserService userService = new UserService(dataAccess);


    public static void init() {
    }

    @BeforeAll
    public static void start() throws DataAccessException {
        dataAccess.init();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess.clear();
        var startingInfo = userService.register(backgroundUser);
    }

    @AfterEach
    public void close() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    public void registerSuccess() {
        try {
            var result = userService.register(baseUser);
            userService.register(secondUser);
            assert dataAccess.totalUsers() == 3;
            assert result.authToken() != null;
            assert Objects.equals(result.username(), baseUser.username());
        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void registerFail() {
        try {
            var result = userService.register(backgroundUser);
            assert dataAccess.totalUsers() == 2;
        }
        catch (Exception ex) {
            assert dataAccess.totalUsers() == 1;
        }
        try {
            userService.register(new UserData(null, "", null));
            assert dataAccess.totalUsers() == 2;
        }
        catch (Exception ex) {
            assert dataAccess.totalUsers() == 1;
        }
    }

    @Test
    public void loginSuccess() {
        try {
            var result = userService.login(backgroundUser);
            assert result.authToken() != null;
            assert Objects.equals(result.username(), backgroundUser.username());
            assert dataAccess.totalUsers() == 1;
            assert dataAccess.totalAuths() == 2;

            var secondResult = userService.register(baseUser);
            assert !Objects.equals(secondResult.authToken(), result.authToken());
            assert !Objects.equals(secondResult.username(), result.username());
            assert dataAccess.totalUsers() == 2;
            assert dataAccess.totalAuths() == 3;
        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void loginFail() {
        try {
            var result = userService.login(backgroundUser);
            userService.login(backgroundUser);
            assert result.authToken() != null;
            assert Objects.equals(result.username(), backgroundUser.username());
        }
        catch (Exception ex) {
            assert dataAccess.totalUsers() == 1;
            assert dataAccess.totalAuths() == 2;
        }
    }

    @Test
    public void logoutSuccess() {
        try {
            var userCodes = userService.login(backgroundUser);
            var result = userService.logout(userCodes.authToken());
            assert dataAccess.totalAuths() == 1;
            assert dataAccess.totalUsers() == 1;

        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void logoutFail() {
        try {
            var userCodes = userService.login(backgroundUser);
            userService.logout(userCodes.authToken());
            userService.logout(userCodes.authToken());
        }
        catch (Exception ex) {
            assert dataAccess.totalAuths() == 1;
            assert dataAccess.totalUsers() == 1;
        }
        try {
            var userCodes = userService.login(backgroundUser);
            userService.logout(userCodes.username());
        }
        catch (Exception ex) {
            assert dataAccess.totalAuths() == 2;
            assert dataAccess.totalUsers() == 1;
        }
        try {
            var userCodes = userService.login(backgroundUser);
            userService.logout(baseUser.username());
        }
        catch (Exception ex) {
            assert dataAccess.totalAuths() == 3;
            assert dataAccess.totalUsers() == 1;
        }
    }

    @Test
    public void listSuccess() {
        try {
            var userData = userService.login(backgroundUser);
            var games = userService.list(userData.authToken());
            assert dataAccess.totalGames() == 0;
            assert games != null;
        }
        catch (Exception ex) {
            assert false;
        }
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            var games = userService.list(userData.authToken());
            assert dataAccess.totalGames() == 1;
        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void listFail() {
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            var games = userService.list(userData.authToken());
            userService.list(userData.username());
        }
        catch (Exception ex) {
            assert dataAccess.totalGames() == 1;
        }
    }

    @Test
    public void createSuccess() {
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            userService.create("hi there", userData.authToken());
            assert dataAccess.totalGames() == 2;
        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void createFail() {
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            var games = userService.list(userData.authToken());
            userService.list(userData.username());
        }
        catch (Exception ex) {
            assert dataAccess.totalGames() == 1;
        }
    }

    @Test
    public void joinSuccess() {
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            var tester = userService.join(firstJoin, userData.authToken());
            assert true;
        }
        catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void joinFail() {
        try {
            var userData = userService.login(backgroundUser);
            userService.create("hello", userData.authToken());
            var tester = userService.join(firstJoin, userData.authToken());
            userService.join(firstJoin, userData.authToken());
        }
        catch (Exception ex) {
            assert true;
        }
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        dataAccess.clear();
        assert dataAccess.totalUsers() == 0;
        assert dataAccess.totalAuths() == 0;
        assert dataAccess.totalGames() == 0;
    }
}
