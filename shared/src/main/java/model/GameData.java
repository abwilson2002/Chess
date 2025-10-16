package model;

import chess.ChessGame;

public record GameData(Double gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
