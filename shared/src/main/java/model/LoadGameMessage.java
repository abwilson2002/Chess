package model;

import chess.ChessPiece;
import websocket.messages.ServerMessage;

import java.util.Map;

public record LoadGameMessage(ServerMessage type, Map<String, ChessPiece> board) {
}
