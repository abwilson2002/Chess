package chess;

import java.util.*;
import java.util.Map;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public Map<ChessPosition, ChessPiece> allPieces = new HashMap<>();

    public ChessBoard() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(allPieces, that.allPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(allPieces);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        allPieces.put(position, piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return allPieces.get(position);
    }

    public void movePiece(ChessPosition startPosition, ChessPosition endPosition, ChessPiece piece) {
        ChessPiece targetLocation = getPiece(endPosition);
        if (targetLocation != null) {
            ChessPiece capturedPiece = allPieces.get(endPosition);  //I am keeping track of this if I need to use it later
            allPieces.remove(endPosition);
            ChessPiece movingPiece = allPieces.get(startPosition);
            allPieces.put(endPosition, piece);
            allPieces.remove(startPosition);
        } else {
            allPieces.put(endPosition, piece);
            allPieces.remove(startPosition);
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        allPieces.clear();
        ChessPiece placeHolder = new ChessPiece(TeamColor.WHITE, PieceType.PAWN);
        int S = 1; //S stands for Side
        int pawnLine = 2;
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 9; j++) {
                this.allPieces.put(new ChessPosition(pawnLine, j),new ChessPiece(placeHolder.getTeamColor(), PieceType.PAWN));
            }
            allPieces.put(new ChessPosition(S,1), new ChessPiece(placeHolder.getTeamColor(), PieceType.ROOK));
            allPieces.put(new ChessPosition(S,2), new ChessPiece(placeHolder.getTeamColor(), PieceType.KNIGHT));
            allPieces.put(new ChessPosition(S,3), new ChessPiece(placeHolder.getTeamColor(), PieceType.BISHOP));
            allPieces.put(new ChessPosition(S,4), new ChessPiece(placeHolder.getTeamColor(), PieceType.QUEEN));
            allPieces.put(new ChessPosition(S,5), new ChessPiece(placeHolder.getTeamColor(), PieceType.KING));
            allPieces.put(new ChessPosition(S,6), new ChessPiece(placeHolder.getTeamColor(), PieceType.BISHOP));
            allPieces.put(new ChessPosition(S,7), new ChessPiece(placeHolder.getTeamColor(), PieceType.KNIGHT));
            allPieces.put(new ChessPosition(S,8), new ChessPiece(placeHolder.getTeamColor(), PieceType.ROOK));
            placeHolder = new ChessPiece(TeamColor.BLACK, PieceType.PAWN);
            S = 8;
            pawnLine = 7;
        }
    }


}
