package service;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;
import passoff.model.*;
import java.util.*;
import org.junit.jupiter.api.Test;

public class MyServiceTests {

    UserData backgroundUser = new UserData("ghostUser", "casper", "shhh");
    UserData baseUser = new UserData("user1", "pass1", "1");
    UserData secondUser = new UserData("user2", "pass2", "2");
    GameData firstGame = new GameData(1.0, null, null, "test", new ChessGame());
    GameData secondGame = new GameData(2.0, null, null, "tester", new ChessGame());


    public static void init() {

    }

    @Test
    public void registerSuccess() {

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
