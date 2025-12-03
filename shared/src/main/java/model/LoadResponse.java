package model;

import chess.ChessPiece;

import java.util.Map;

public record LoadResponse(Map<String, ChessPiece> board) {
}
