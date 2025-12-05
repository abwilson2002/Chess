package webmodel;

import chess.ChessMove;
import chess.ChessPiece;
import websocket.messages.ServerMessage;
import java.util.Collection;
import java.util.Map;

public record HighlightMessage(ServerMessage.ServerMessageType serverMessageType, Map<String, ChessPiece> board, Collection<ChessMove> moves) {
}
