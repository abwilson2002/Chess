package webmodel;

import chess.ChessMove;
import chess.ChessPiece;
import java.util.Collection;
import java.util.Map;

public record HighlightResponse(Map<String, ChessPiece> allPieces, Collection<ChessMove> moves) {
}
