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
public class ChessBoard implements Cloneable{

    public Map<ChessPosition, ChessPiece> allPieces = new HashMap<>();
    boolean whitePawnDoubleMove = false;
    boolean blackPawnDoubleMove = false;
    public ChessPosition enPassantPosition;

    public ChessBoard() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return whitePawnDoubleMove == that.whitePawnDoubleMove && blackPawnDoubleMove == that.blackPawnDoubleMove && Objects.equals(allPieces, that.allPieces) && Objects.equals(enPassantPosition, that.enPassantPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allPieces, whitePawnDoubleMove, blackPawnDoubleMove, enPassantPosition);
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

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for (int i = 8; i > 0; i--) {
            boardString.append("|");
            for (int j = 1; j < 9; j++) {
                boardString.append(pieceString(new ChessPosition(i,j)));
                boardString.append("|");
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

    private String pieceString(ChessPosition position) {
        ChessPiece temp = allPieces.get(position);
        if (temp == null) {
            return " ";
        }
        if (temp.getTeamColor() == TeamColor.WHITE) {
            if (temp.getPieceType() == PieceType.PAWN) {
                return "P";
            } else if (temp.getPieceType() == PieceType.ROOK) {
                return "R";
            } else if (temp.getPieceType() == PieceType.BISHOP) {
                return "B";
            } else if (temp.getPieceType() == PieceType.KNIGHT) {
                return "N";
            } else if (temp.getPieceType() == PieceType.KING) {
                return "K";
            } else if (temp.getPieceType() == PieceType.QUEEN) {
                return "Q";
            }
        } else {
            if (temp.getPieceType() == PieceType.PAWN) {
                return "p";
            } else if (temp.getPieceType() == PieceType.ROOK) {
                return "r";
            } else if (temp.getPieceType() == PieceType.BISHOP) {
                return "b";
            } else if (temp.getPieceType() == PieceType.KNIGHT) {
                return "n";
            } else if (temp.getPieceType() == PieceType.KING) {
                return "k";
            } else if (temp.getPieceType() == PieceType.QUEEN) {
                return "q";
            }
        }
        return " ";
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard clone = (ChessBoard) super.clone();
            clone.allPieces = new HashMap<ChessPosition, ChessPiece>();
            for (Map.Entry<ChessPosition, ChessPiece> piece : this.allPieces.entrySet()) {
                clone.allPieces.put(piece.getKey(), piece.getValue().clone());
            }
            clone.enPassantPosition = this.enPassantPosition;
            clone.whitePawnDoubleMove = this.whitePawnDoubleMove;
            clone.blackPawnDoubleMove = this.blackPawnDoubleMove;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
