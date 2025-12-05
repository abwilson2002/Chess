package webmodel;

import chess.ChessPiece;

import java.util.Map;

public record MoveResponse(Map<String, ChessPiece> board, String user, String gameState) {
}
