package model;

import chess.ChessMove;
import chess.ChessPiece;
import websocket.messages.ServerMessage;
import java.util.Collection;
import java.util.Map;

public record HighlightMessage(ServerMessage.ServerMessageType type, Map<String, ChessPiece> board, Collection<ChessMove> moves) {
}
