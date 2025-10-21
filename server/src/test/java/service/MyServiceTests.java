package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.*;
import passoff.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;

public class MyServiceTests {

    static UserData backgroundUser = new UserData("ghostUser", "casper", "shhh");
    static UserData baseUser = new UserData("user1", "pass1", "1");
    static UserData secondUser = new UserData("user2", "pass2", "2");
    static GameData firstGame = new GameData(1.0, null, null, "test", new ChessGame());
    static GameData secondGame = new GameData(2.0, null, null, "tester", new ChessGame());
    static DataAccess dataAccess = new MemoryDataAccess();


    public static void init() {
        dataAccess.addAuth(backgroundUser.username());
    }

    @AfterAll
    public static void close() {
        dataAccess.clear();
    }

    @Test
    public void registerSuccess() {
        dataAccess.addAuth(baseUser.username());
        assert dataAccess.totalUsers() == 2;
    }

    @Test
    public void registerFail() {

    }

    @Test
    public void loginSuccess() {

    }

    @Test
    public void loginFail() {

    }

    @Test
    public void logoutSuccess() {

    }

    @Test
    public void logoutFail() {

    }

    @Test
    public void listSuccess() {

    }

    @Test
    public void listFail() {

    }

    @Test
    public void createSuccess() {

    }

    @Test
    public void createFail() {

    }

    @Test
    public void joinSuccess() {

    }

    @Test
    public void joinFail() {

    }

    @Test
    public void clearSuccess() {

    }
}
