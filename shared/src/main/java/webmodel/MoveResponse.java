package webmodel;

import chess.ChessPiece;

import java.util.Map;

public record MoveResponse(Map<String, ChessPiece> game, String user, String gameState) {
}
