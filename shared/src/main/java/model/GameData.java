package model;

import chess.ChessGame;

public class GameData {

    Integer gameID;
    String whiteUsername;
    String blackUsername;
    String gameName;
    ChessGame game;


    public GameData(Integer gameID, String gameName, ChessGame game) {
        this.gameID = gameID;
        whiteUsername = null;
        blackUsername = null;
        this.gameName = gameName;
        this.game = game;
    }
}
