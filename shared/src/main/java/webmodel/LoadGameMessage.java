package webmodel;

import chess.ChessPiece;
import websocket.messages.ServerMessage;

import java.util.Map;

public record LoadGameMessage(ServerMessage.ServerMessageType serverMessageType, Map<String, ChessPiece> game) {
}
